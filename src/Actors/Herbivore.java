package Actors;

import Inanimate.*;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

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

    /** Daytime - move randomly and eat if hungry
     *
     *@param world the world in which the herbivore exists
     */
    public void dayBehaviour(World world) {
        Location moveTo = moveRandomly(world);
        if (moveTo != null && isHungry()) {
            eat(world, moveTo);
        }
    }

    /** Nighttime - sleep if in shelter, else lose energy
     *
     * @param world the world in which the herbivore exists
     */
    public void nightBehaviour(World world) {
        if (hasShelter()) sleep(world);
        else energy -= 5;
    }

    // ----------- LIFE -----------

    /** Check if the herbivore has shelter.
     *
     * @return true if the herbivore has shelter, false otherwise
     */
    protected boolean hasShelter() { return shelter != null; }

    /** Seek shelter by moving towards it.
     *
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
        Object nb = world.getNonBlocking(targetLoc); // græs/busk ligger typisk som non-blocking
        if (canEat(nb)) {
            energy += getFoodEnergy(nb);

            if (nb instanceof Grass) {
                world.delete(nb);
            } else if (nb instanceof Bush bush) {
                bush.berriesEaten(); // hvis den kan spise bær
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
