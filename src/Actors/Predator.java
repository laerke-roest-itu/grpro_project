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

    /**
     * Returns the hunting area of the predator.
     *
     * @param world the world in which the predator hunts
     * @return the set of locations defining the hunting area
     */
    protected abstract Set<Location> getHuntingArea(World world);

    /**
     * Returns the movement cost for hunting.
     *
     * @return the movement cost as an integer
     */
    protected abstract int getHuntMoveCost();

    /**
     * Determines if another animal is an enemy predator.
     *
     * @param other the other animal to check
     * @return true if the other animal is an enemy predator, false otherwise
     */
    protected abstract boolean isEnemyPredator(Animal other);

    /**
     * The hunting behavior of the predator.
     * It checks for nearby herbivore to kill, otherwise it looks for enemy predators, carcasses, or prey in its hunting area.
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

    /**
     * Engages the target at the specified location. If adjacent, it interacts (kills or eats).
     * Otherwise, it moves one step closer.
     * @param world the world in which the predator acts
     * @param targetLoc the location of the target to engage
     */
    protected void engageTarget(World world, Location targetLoc) {
        Location myLoc = world.getLocation(this);
        Set<Location> neighbors = world.getSurroundingTiles(myLoc);

        if (neighbors.contains(targetLoc)) {
            Object o = world.getTile(targetLoc);

            if (o instanceof Herbivore prey) {
                kill(world, prey);
            }
            if (o instanceof Predator predator && isEnemyPredator(predator)) {
                fight(predator, world);
                return;
            }

            eat(world, targetLoc);
        } else {
            moveOneStepTowards(world, targetLoc, getHuntMoveCost());
        }
    }


    /**
     * Find the closest enemy predator within a specified area.
     * @param world the world in which the predator exists
     * @param area the set of locations to search in
     * @param from the starting location for distance calculation
     * @return the location of the closest enemy predator, or null if none found
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
     * @param world the world in which the predator exists
     * @param area the set of locations to search in
     * @param from the starting location for distance calculation
     * @return the location of the closest carcass, or null if none found
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
     * @param world the world in which the predator exists
     * @param area the set of locations to search in
     * @param from the starting location for distance calculation
     * @return the location of the closest prey, or null if none found
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
     * @return the attack damage value
     */
    protected abstract int getAttackDamage();

    /**
     * Get the attack damage against a specific target.
     * @param target the animal being attacked
     * @return the amount of damage to deal
     */
    public int getAttackDamageAgainst(Animal target) {
        return getAttackDamage();
    }

    
    /**
     * Kills the specified prey in the world.
     * @param world the world in which the kill occurs.
     * @param prey the animal to be killed.
     */
    protected void kill(World world, Animal prey) {
        prey.die(world);
    }
}
