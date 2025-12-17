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

        Set<Location> neighbors = world.getSurroundingTiles(myLoc);
        for (Location loc : neighbors) {
            Object obj = world.getTile(loc);
            if (obj instanceof Herbivore) {
                kill(world, (Herbivore) obj);
                return;
            }
        }

        Set<Location> area = getHuntingArea(world);

        Location enemyLoc = findClosestEnemyPredator(world, area, myLoc);
        if (enemyLoc != null) {
            engageTarget(world, enemyLoc);
            return;
        }

        Location carcassLoc = findClosestCarcass(world, area, myLoc);
        if (carcassLoc != null) {
            engageTarget(world, carcassLoc);
            return;
        }


        Location preyLoc = findClosestPrey(world, area, myLoc);
        if (preyLoc != null) {
            engageTarget(world, preyLoc);
        }
    }

    protected void engageTarget(World world, Location targetLoc) {
        Location myLoc = world.getLocation(this);
        Set<Location> neighbors = world.getSurroundingTiles(myLoc);

        if (neighbors.contains(targetLoc)) {
            // we're next to target -> interact
            Object o = world.getTile(targetLoc);

            if (o instanceof Herbivore prey) {
                kill(world, prey);
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
     * Find the closest enemy predator within a specified area.
     * @param world
     * @param area
     * @param from
     * @return
     */

    protected Location findClosestEnemyPredator(World world, Set<Location> area, Location from) {
        Location closest = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Location loc : area) {
            Object obj = world.getTile(loc);
            if (obj instanceof Predator predator) {
                if (isEnemyPredator(predator)) {
                    int d = distance(from, loc);
                    if (d < bestDistance) {
                        bestDistance = d;
                        closest = loc;
                    }
                }
            }
        }
        return closest;
    }

    /**
     * Find the closest carcass within a specified area.
     * @param world
     * @param area
     * @param from
     * @return
     */

    protected Location findClosestCarcass(World world, Set<Location> area, Location from) {
        Location closest = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Location loc : area) {
            Object obj = world.getTile(loc);
            if (obj instanceof Carcass) {
                int d = distance(from, loc);
                if (d < bestDistance) {
                    bestDistance = d;
                    closest = loc;
                }
            }
        }
        return closest;
    }

    /**
     * Find the closest prey within a specified area.
     * @param world
     * @param area
     * @param from
     * @return
     */
    protected Location findClosestPrey(World world, Set<Location> area, Location from) {
        Location closest = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Location loc : area) {
            Object obj = world.getTile(loc);
            if (obj instanceof Animal && !(obj instanceof Predator)) {
                int d = distance(from, loc);
                if (d < bestDistance) {
                    bestDistance = d;
                    closest = loc;
                }
            }
        }

        return closest;
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

    /** The predator kills its prey
     *
     * @param world is the world in which the prey dies
     * @param prey is the animal that gets killed
     */
    protected void kill(World world, Animal prey) {
        prey.die(world);
    }

}
