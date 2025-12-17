package Actors;

import itumulator.world.Location;
import itumulator.world.World;
import java.util.function.Predicate;

import java.util.Set;

/**
 * Predator is an abstract class representing carnivorous animals that hunt other animals.
 * It extends the Animal class and provides a general hunting algorithm.
 */
public abstract class Predator extends Animal {

    /**
     * Constructor for Predator.
     *
     * @param maxAge the maximum age of the predator
     */
    protected Predator(int maxAge) {
        super(maxAge);
    }

    /** Returns the hunting area of the predator.
     *
     * @param world hswo
     * @return the set of locations defining the hunting area
     */
    protected abstract Set<Location> getHuntingArea(World world);

    /** Returns the movement cost for hunting.
     *
     * @return the movement cost as an integer
     */
    protected abstract int getHuntMoveCost();

    /** Determines if another animal is an enemy predator.
     *
     * @param other the other animal to check
     * @return true if the other animal is an enemy predator, false otherwise
     */
    protected abstract boolean isEnemyPredator(Animal other);

    /** The hunting behavior of the predator.
     *
     * @param world the world in which the predator exists
     */
    protected void hunt(World world) {
        Location myLoc = world.getLocation(this);
        Set<Location> area = getHuntingArea(world);

        Location enemyLoc = findClosestEnemyPredator(world, area, myLoc);
        if (enemyLoc != null) {
            engageTarget(world, enemyLoc);
            return;
        }

        if (isHungry()) {
            Location carcassLoc = findClosestCarcass(world, area, myLoc);
            if (carcassLoc != null) {
                engageTarget(world, carcassLoc);
                return;
            }
        }

        Location preyLoc = findClosestPrey(world, area, myLoc);
        if (preyLoc != null) {
            engageTarget(world, preyLoc);
        }
    }

    /**
     *
     */
    protected void engageTarget(World world, Location targetLoc) {
        Location myLoc = world.getLocation(this);
        Set<Location> neighbors = world.getSurroundingTiles(myLoc);

        if (neighbors.contains(targetLoc)) {
            // we're next to target -> interact
            Object o = world.getTile(targetLoc);

            if (o instanceof Rabbit rabbit) {
                kill(world, rabbit);
            }
            if (o instanceof Predator predator && isEnemyPredator(predator)) {
                fight(predator, world);
                // if enemy died during fight, we can possibly eat the Carcass during a later tick
                return;
            }

            // 2) or: there's something we can eat (Carcass, Rabbit, osv.)
            eat(world, targetLoc);
        } else {
            // or: move one step closer to target
            moveOneStepTowards(world, targetLoc, getHuntMoveCost());
        }
    }

    /**
     * Small help method to find the nearest prey
     * @param world
     * @param area
     * @param from
     * @param matcher
     * @return
     */

    protected Location findClosestMatching(World world, Set<Location> area, Location from, Predicate<Object> matcher) {

        Location best = null;
        int bestDist = Integer.MAX_VALUE;

        for (Location loc : area) {
            Object o = world.getTile(loc);
            if (o == null) continue;
            if (o == this) continue;

            // here we use the filter we've gotten
            if (!matcher.test(o)) continue;

            int d = distance(from, loc);
            if (d < bestDist) {
                bestDist = d;
                best = loc;
            }
        }

        return best;
    }

    /**
     * Find the closest enemy predator within a specified area.
     * @param world
     * @param area
     * @param from
     * @return
     */

    protected Location findClosestEnemyPredator(World world, Set<Location> area, Location from) {
        return findClosestMatching(world, area, from, o ->
                o instanceof Predator p && isEnemyPredator(p)
        );
    }

    /**
     * Find the closest carcass within a specified area.
     * @param world
     * @param area
     * @param from
     * @return
     */

    protected Location findClosestCarcass(World world, Set<Location> area, Location from) {
        return findClosestMatching(world, area, from, o ->
                o instanceof Carcass
        );
    }

    /**
     * Find the closest prey within a specified area.
     * @param world
     * @param area
     * @param from
     * @return
     */

    protected Location findClosestPrey(World world, Set<Location> area, Location from) {
        return findClosestMatching(world, area, from, o ->
                o instanceof Animal && !(o instanceof Predator)
        );
    }

    /**
     * Engage in a fight with another predator.
     * @param opponent the opposing predator.
     * @param world the world in which the fight takes place.
     */

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

    /**
     * Get the base attack damage of the predator.
     * @return
     */

    protected abstract int getAttackDamage();         // base-damage

    /**
     * Get the attack damage against a specific target.
     * @param target
     * @return
     */

    public int getAttackDamageAgainst(Animal target) {
        // standard: same damage to all targets
        return getAttackDamage();
    }

    /**
     *  Kill the specified prey animal in the world. And insure if it dies and creates a Carcass,
     *  the Predator can eat it afterwards.
     * @param world
     * @param prey
     */

    protected void kill(World world, Animal prey) {
        prey.die(world); // if die() creates a Carcass, Predators can eat() it afterwards
    }

}
