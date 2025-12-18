package Actors;

import Inanimate.Group;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Abstract class representing a generic Animal in the simulation.
 * Implements common behaviors and properties for all animals.
 */
public abstract class Animal implements Actor, DynamicDisplayInformationProvider {
    protected int age;
    protected int maxAge;
    protected int energy;
    public boolean isAlive;
    public boolean isSleeping;
    protected int amountOfKids;
    protected Object shelter;
    protected Random random;
    protected Group<? extends Animal> group;

    /**
     * Constructor for Animal class.
     *
     * @param maxAge The maximum age the animal can reach.
     */
    public Animal(int maxAge) {
        this.age = 0;
        this.maxAge = maxAge;
        this.energy = 100;
        this.isAlive = true;
        this.isSleeping = false;
        this.amountOfKids = 0;
        this.random = new Random();
    }

    // ----------- ACT -----------

    /**
     * The act method is called on each tick of the simulation.
     * It updates the animal's state if it is alive and not sleeping.
     *
     * @param world The world in which the animal acts.
     */
    @Override
    public void act(World world) {
        if (!isAlive) return;

        if (getAge() >= getMaxAge() || getEnergy() <= 0) {
            die(world);
            return;
        }

        tickCommon();

        if (world.isDay() && isSleeping) {
            wakeUp(world);
        }

        if (isSleeping) return;

        if (world.getCurrentTime() >= World.getTotalDayDuration() - 3) {
            seekShelter(world);
        }

        if (world.isNight()) {
            nightBehaviour(world);
            return;
        }

        if (world.isDay()) {
            dayBehaviour(world);
        }
    }

    /**
     * Determines the night behavior of the animal.
     * Called during the act method if it is currently night in the world.
     *
     * @param world The world in which the animal acts.
     */
    public abstract void nightBehaviour(World world);

    /**
     * Determines the day behavior of the animal.
     * Called during the act method if it is currently day in the world.
     *
     * @param world The world in which the animal acts.
     */
    public abstract void dayBehaviour(World world);

    /**
     * Common tick updates for all animals.
     * Increases age and decreases energy.
     */
    protected void tickCommon() {
        age++;
        energy--;
    }

    /**
     * Moves the animal randomly to an adjacent empty tile.
     *
     * @param world The world in which the animal moves.
     * @return The new location of the animal after moving, or null if no move was possible.
     */
    protected Location moveRandomly(World world) {
        Location animalLocation = world.getLocation(this);
        Set<Location> emptyTilesNearAnimal = world.getEmptySurroundingTiles(animalLocation);

        if (emptyTilesNearAnimal.isEmpty()) return null;
        List<Location> listOfPlacesToMove = new ArrayList<>(emptyTilesNearAnimal);
        int j = random.nextInt(emptyTilesNearAnimal.size());
        Location moveTo = listOfPlacesToMove.get(j);
        world.move(this, moveTo);
        energy -= 5;

        return moveTo;
    }

    /**
     * Moves the animal one step towards a target location.
     *
     * @param world      The world in which the animal moves.
     * @param target     The target location to move towards.
     * @param energyCost The energy cost of moving.
     */
    protected void moveOneStepTowards(World world, Location target, int energyCost) {
        Location currentLoc = world.getLocation(this);
        if (currentLoc == null || target == null) return;

        Set<Location> emptyNeighbors = world.getEmptySurroundingTiles(currentLoc);
        if (emptyNeighbors.isEmpty()) return;

        Location bestMove = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Location loc : emptyNeighbors) {
            int d = distance(loc, target);
            if (d < bestDistance) {
                bestDistance = d;
                bestMove = loc;
            }
        }

        if (bestMove != null) {
            world.move(this, bestMove);
            energy -= energyCost;
        }
    }

    /**
     * Moves the animal one step towards a target location with a default energy cost.
     *
     * @param world  The world in which the animal moves.
     * @param target The target location to move towards.
     */
    protected void moveOneStepTowards(World world, Location target) {
        moveOneStepTowards(world, target, 5);
    }

    /**
     * Seeks shelter by moving towards the shelter location.
     *
     * @param world The world in which the animal seeks shelter.
     */
    public void seekShelter(World world) {
        if (shelter == null) return;

        Location myLoc = world.getLocation(this);
        Location shelterLoc = world.getLocation(shelter);

        if (myLoc.equals(shelterLoc)) {
            handleSleepLocation(world);
            return;
        }

        moveOneStepTowards(world, shelterLoc);
    }

    // ----------- LIFE -----------

    /**
     * Handles the death of the animal.
     * Replaces the animal with a carcass in the world.
     *
     * @param world The world in which the animal dies.
     */
    public void die(World world) {
        isAlive = false;

        if (group != null) {
            ((Group) group).removeMember(this);
        }

        Location loc = world.getLocation(this);
        if (loc != null) {
            int meat = getMeatValue();
            int rot = 25;
            Carcass carcass = new Carcass(meat, rot);

            world.delete(this);
            world.setTile(loc, carcass);
        }
    }

    /**
     * Puts the animal to sleep, increasing its energy.
     *
     * @param world The world in which the animal sleeps.
     */
    public void sleep(World world) {
        isSleeping = true;
        handleSleepLocation(world);
        energy += getSleepEnergy();
    }

    /**
     * Handles the specific sleep location logic for the animal.
     * Each animal type handles its own sleep location (e.g., in a burrow or den).
     *
     * @param world The world in which the animal handles its sleep location.
     */
    protected abstract void handleSleepLocation(World world);

    /**
     * Gets the amount of energy restored when the animal sleeps.
     *
     * @return The energy restored from sleeping.
     */
    protected int getSleepEnergy() {
        return 50;
    }

    /**
     * Wakes the animal up from sleep.
     *
     * @param world The world in which the animal wakes up.
     */
    public void wakeUp(World world) {
        isSleeping = false;
    }

    // ----------- EATING -----------

    /**
     * Eats the object at the target location if it can be eaten.
     * The method is empty as each animal overrides.
     * @param world     The world in which the animal eats.
     * @param targetLoc The location of the object to eat.
     */
    public void eat(World world, Location targetLoc) {}

    /**
     * Checks if the animal can eat the given object.
     *
     * @param object The object to check.
     * @return True if the animal can eat the object, false otherwise.
     */
    protected abstract boolean canEat(Object object);

    /**
     * Gets the amount of energy gained from eating the given object.
     *
     * @param object The object being eaten.
     * @return The energy gained from the food.
     */
    protected abstract int getFoodEnergy(Object object);

    /**
     * Gets the meat value of the animal when it dies.
     *
     * @return The meat value.
     */
    protected abstract int getMeatValue();

    /**
     * Checks if the animal is hungry based on its energy level.
     *
     * @return True if the animal is hungry, false otherwise.
     */
    protected boolean isHungry() {
        return getEnergy() < 50;
    }

    // ----------- REPRODUCTION -----------

    /**
     * Reproduce if conditions are met (sufficient energy and not a child).
     * Creates a child animal at a suitable location in the world.
     *
     * @param world The world in which the animal reproduces.
     */
    public void reproduce(World world) {
        if (energy < 30 || (isChild()) || getAmountOfKids()>2) return;
        Location loc = getReproductionLocation(world);
        if (loc != null) {
            Animal child = createChild(world, loc);
            world.setTile(loc, child);
            amountOfKids++;
            energy -= 15;
        }
    }

    /**
     * Creates a child animal at the specified location.
     *
     * @param world    The world in which the child is created.
     * @param childLoc The location for the child animal.
     * @return The newly created child animal.
     */
    protected abstract Animal createChild(World world, Location childLoc);

    /**
     * Gets a suitable location for reproduction.
     *
     * @param world The world in which to find the reproduction location.
     * @return The location for reproduction, or null if no suitable location is found.
     */
    protected abstract Location getReproductionLocation(World world);

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    /**
     * Checks if the animal is considered a child based on its age.
     *
     * @return True if the animal is a child, false otherwise.
     */
    public boolean isChild() {
        return getAge() < 50;
    }

    /**
     * Checks if the animal is alive.
     *
     * @return True if the animal is alive, false otherwise.
     */
    public boolean isAlive() { return isAlive; }

    /**
     * Calculates the Manhattan distance between two locations.
     *
     * @param a The first location.
     * @param b The second location.
     * @return The Manhattan distance between the two locations.
     */
    public int distance(Location a, Location b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    /**
     * Gets the current age of the animal.
     *
     * @return The age of the animal.
     */
    public int getAge() { return age; }

    /**
     * Gets the maximum age of the animal.
     *
     * @return The maximum age of the animal.
     */
    public int getMaxAge() { return maxAge;}

    /**
     * Gets the current energy level of the animal.
     *
     * @return The energy level of the animal.
     */
    public int getEnergy() { return energy; }

    /**
     * Sets the energy level of the animal.
     *
     * @param i The new energy level.
     */
    public void setEnergy(int i) {
        energy = i;
    }

    /**
     * Gets the amount of kids the animal has.
     *
     * @return The number of kids.
     */
    public int getAmountOfKids() { return amountOfKids; }

    /**
     * Sets the group for the animal.
     * @param group The group to assign the animal to.
     */
    public void setGroup(Group<? extends Animal> group) {
        this.group = group;
    }

}
