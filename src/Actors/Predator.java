package Actors;

import itumulator.world.Location;
import itumulator.world.World;
import java.util.function.Predicate;

import java.util.Set;

public abstract class Predator extends Animal {

    // 1) Hvilket område jager dette rovdyr i?
    //    Bjørn: territoriet. Ulv: felter omkring ulven (radius 2).
    protected abstract Set<Location> getHuntingArea(World world);

    // 2) Hvad koster det i energi at gå ét skridt når vi jager?
    protected abstract int getHuntMoveCost();

    // 3) Hvilke dyr er "fjender" vi kan slås med?
    protected abstract boolean isEnemyPredator(Animal other);

    // GENEREL JAGT-ALGORITME
    protected void hunt(World world) {
        Location myLoc = world.getLocation(this);
        Set<Location> area = getHuntingArea(world);

        // 1) PRIORITET: fjendtligt rovdyr
        Location enemyLoc = findClosestEnemyPredator(world, area, myLoc);
        if (enemyLoc != null) {
            engageTarget(world, enemyLoc);
            return;
        }

        // 2) PRIORITET: ådsel, men kun hvis vi er sultne
        if (isHungry()) {
            Location carcassLoc = findClosestCarcass(world, area, myLoc);
            if (carcassLoc != null) {
                engageTarget(world, carcassLoc);
                return;
            }
        }

        // 3) PRIORITET: levende bytte
        Location preyLoc = findClosestPrey(world, area, myLoc);
        if (preyLoc != null) {
            engageTarget(world, preyLoc);
        }
        // ellers: ingen mål → gør ingenting denne tur
    }

    protected boolean isHungry() {
        return getEnergy() < 50;
    }

    protected void engageTarget(World world, Location targetLoc) {
        Location myLoc = world.getLocation(this);
        Set<Location> neighbors = world.getSurroundingTiles(myLoc);

        if (neighbors.contains(targetLoc)) {
            // vi står ved siden af målet → interager
            Object o = world.getTile(targetLoc);

            if (o instanceof Rabbit rabbit) {
                kill(world, rabbit);
            }
            if (o instanceof Predator predator && isEnemyPredator(predator)) {
                fight(predator, world);
                // hvis fjenden døde i kampen, kan vi evt. spise ådslet i en senere tur
                return;
            }

            // 2) ellers: det er noget vi kan spise (Carcass, Rabbit, osv.)
            eat(world, targetLoc);
        } else {
            // ellers: bevæg os ét skridt tættere på målet
            moveOneStepTowards(world, targetLoc, getHuntMoveCost());
        }
    }


    // lille hjælpe-metode til at finde nærmeste bytte
    protected Location findClosestMatching(World world, Set<Location> area, Location from, Predicate<Object> matcher) {

        Location best = null;
        int bestDist = Integer.MAX_VALUE;

        for (Location loc : area) {
            Object o = world.getTile(loc);
            if (o == null) continue;
            if (o == this) continue;

            // her bruger vi det filter vi har fået
            if (!matcher.test(o)) continue;

            int d = distance(from, loc);
            if (d < bestDist) {
                bestDist = d;
                best = loc;
            }
        }

        return best;
    }

    protected Location findClosestEnemyPredator(World world, Set<Location> area, Location from) {
        return findClosestMatching(world, area, from, o ->
                o instanceof Predator p && isEnemyPredator(p)
        );
    }

    protected Location findClosestCarcass(World world, Set<Location> area, Location from) {
        return findClosestMatching(world, area, from, o ->
                o instanceof Carcass
        );
    }

    protected Location findClosestPrey(World world, Set<Location> area, Location from) {
        return findClosestMatching(world, area, from, o ->
                o instanceof Animal && !(o instanceof Predator)
        );
    }


    protected void fight(Predator opponent, World world) {
        int myDamage      = this.getAttackDamageAgainst(opponent);
        int theirDamage   = opponent.getAttackDamageAgainst(this);

        opponent.energy -= myDamage;
        this.energy     -= theirDamage;

        if (opponent.energy <= 0) {
            opponent.die(world);
        }
        if (this.energy <= 0) {
            this.die(world);
        }
    }


    protected abstract int getAttackDamage();         // grund-damage

    protected int getAttackDamageAgainst(Animal target) {
        // standard: samme mod alle
        return getAttackDamage();
    }


    // KILL-ABSTRAKTION
    protected void kill(World world, Animal prey) {
        prey.die(world); // hvis die() laver Carcass, kan rovdyr spise den bagefter
    }

}
