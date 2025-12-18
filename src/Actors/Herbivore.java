package Actors;

import Inanimate.*;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;
import java.util.Set;

/**
 * An abstract class representing a herbivorous animal.
 */
public abstract class Herbivore extends Animal {

    /**
     * Constructor for Herbivore with specified maximum age.
     *
     * @param maxAge the maximum age of the herbivore
     */
    protected Herbivore(int maxAge) {
        super(maxAge);
    }

    // ----------- ACT -----------

    /**
     * Defines the behavior of the herbivore during the day.
     * The herbivore moves randomly and eats if it is hungry and food is available on its tile or nearby.
     *
     * @param world the world in which the herbivore exists
     */
    @Override
    public void dayBehaviour(World world) {
        Location myLoc = world.getLocation(this);
        if (myLoc == null) return;

        Object onTile = world.getNonBlocking(myLoc);
        if (isHungry() && canEat(onTile)) {
            eat(world, myLoc);
        }

        if (isHungry()) {
            Location nearestFood = findNearestFood(world, myLoc, 2);
            if (nearestFood != null) {
                if (distance(myLoc, nearestFood) == 1 && world.isTileEmpty(nearestFood)) {
                    world.move(this, nearestFood);
                    energy -= 5;
                    eat(world, nearestFood);
                } else {
                    moveOneStepTowards(world, nearestFood);
                }
                return;
            }
        }
        moveRandomly(world);
    }

    /**
     * Defines the behavior of the herbivore during the night.
     * The herbivore sleeps if it is in or near its shelter, otherwise it loses energy.
     *
     * @param world the world in which the herbivore exists
     */
    @Override
    public void nightBehaviour(World world) {
        if (hasShelter()) {
            Location myLoc = world.getLocation(this);
            Location shelterLoc = null;
            try {
                shelterLoc = world.getLocation(shelter);
            } catch (IllegalArgumentException e) {
            }

            if (myLoc != null && shelterLoc != null && distance(myLoc, shelterLoc) <= 1) {
                sleep(world);
            }
        } else {
            energy -= 5;
        }
    }

    /**
     * Finds the nearest food within the given radius.
     *
     * @param world  the world to search in
     * @param myLoc  the current location of the herbivore
     * @param radius the search radius
     * @return the location of the nearest food, or null if none found
     */
    protected Location findNearestFood(World world, Location myLoc, int radius) {
        Location nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (int r = 1; r <= radius; r++) {
            Set<Location> neighbors = world.getSurroundingTiles(myLoc, r);
            for (Location n : neighbors) {
                Object nb = world.getNonBlocking(n);
                if (canEat(nb)) {
                    int dist = distance(myLoc, n);
                    if (dist < minDistance) {
                        minDistance = dist;
                        nearest = n;
                    }
                }
            }
            if (nearest != null) break; // Found something in the smallest radius possible
        }
        return nearest;
    }

    // ----------- LIFE -----------

    /**
     * Checks if the herbivore has a shelter assigned.
     *
     * @return true if the herbivore has shelter, false otherwise
     */
    protected boolean hasShelter() { return shelter != null; }

    /**
     * Wakes up the herbivore. Empty as each herbivore overrides if needed.
     * @param world the world in which the herbivore exists
     */
    @Override
    protected void handleSleepLocation(World world) {
    }

    // ----------- EATING -----------

    /**
     * Check if the herbivore can eat the given object.
     *
     * @param object the object to check
     * @return true if the herbivore can eat the object, false otherwise
     */
    @Override
    protected boolean canEat(Object object) {
        if (object instanceof Grass) return true;
        if (object instanceof Bush bush) return bush.hasBerries();
        return false;
    }

    /**
     * Eat the food at the target location in the world.
     *
     * @param world     the world in which the herbivore exists
     * @param targetLoc the location of the food to eat
     */
    @Override
    public void eat(World world, Location targetLoc) {
        Object nb = world.getNonBlocking(targetLoc);
        if (canEat(nb)) {
            energy += getFoodEnergy(nb);

            if (nb instanceof Grass) {
                world.delete(nb);
            } else if (nb instanceof Bush bush) {
                bush.berriesEaten();
            }
        }
    }

    /**
     * Get the food energy provided by the given object.
     *
     * @param object the object to get food energy from
     * @return the food energy value
     */
    @Override
    protected int getFoodEnergy(Object object) {
        if (object instanceof Grass) return 20;
        if (object instanceof Bush bush && bush.hasBerries()) return bush.getBerryCount() * 2;
        return 0;
    }

    /**
     * Herbivores (abstract) do not provide meat when they die.
     *
     * @return 0 as herbivores do not provide meat
     */
    @Override
    protected int getMeatValue() {
        return 0;
    }

    // ----------- REPRODUCTION -----------

    /**
     * Herbivores (abstract) does not create a child.
     *
     * @param world    the world in which the herbivore exists
     * @param childLoc the location where the child will be created
     * @return null
     */
    @Override
    protected Animal createChild(World world, Location childLoc) {
        return null;
    }

    /**
     * Herbivores (abstract) do not have a specific reproduction location.
     *
     * @param world the world in which the herbivore exists
     * @return null
     */
    @Override
    protected Location getReproductionLocation(World world) {
        return null;
    }

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    /**
     * Herbivores (abstract) do not provide display information.
     *
     * @return null
     */
    @Override
    public DisplayInformation getInformation() {
        return null;
    }
}
