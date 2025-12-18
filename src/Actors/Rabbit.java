package Actors;

import Inanimate.Burrow;
import Inanimate.Fungi;
import Inanimate.Grass;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Rabbit represents a herbivorous animal that can dig and live in burrows.
 * It has behaviors for day and night, including eating grass,
 * seeking shelter in burrows, and reproducing.
 */
public class Rabbit extends Herbivore {
    private Burrow burrow;

    /**
     * Constructor for Rabbit.
     */
    public Rabbit() {
        super(180);
    }

    /**
     * Constructor for Rabbit with specified Random instance. Used for testing.
     *
     * @param random the Random instance to use for randomness
     */
    public Rabbit(Random random) {
        super(180);
        this.random = random;
    }

    // ----------- ACT -----------

    @Override
    public void act(World world) {
        if (!isAlive) return;
        super.act(world);
    }

    /**
     * Rabbit's "act" method, handling day/night behavior and life cycle.
     * Rabbit calls superclass act method (as rabbit does not have an act-method)
     * and adds burrow-related behavior during the day.
     * @param world the world in which the rabbit exists
     */
    @Override
    public void dayBehaviour(World world) {
        super.dayBehaviour(world);

        if (burrow == null) {
            double r = random.nextDouble();
            if (r < 0.10) digBurrow(world);
            else claimBurrow(world);
        }
    }

    /**
     * Determines the rabbit's behavior at night.
     * The rabbit will sleep if it has a burrow and attempt to reproduce if other rabbits are present.
     *
     * @param world the world in which the rabbit exists
     */
    @Override
    public void nightBehaviour(World world) {
        if (burrow != null) {
            sleep(world);
            List<Rabbit> loveRabbits = burrow.getRabbits();
            if (loveRabbits.size() >= 2) {
                reproduce(world);
            }
        } else {
            energy -= 5; // no shelter - lose energy from exposure
        }
    }

    /**
     * Rabbit seeks shelter in its burrow.
     *
     * @param world the world in which the rabbit exists
     */
    @Override
    public void seekShelter(World world) {
        if (burrow == null) return;

        Location burrowLoc;
        try {
            burrowLoc = world.getLocation(burrow);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (burrowLoc == null) return;

        moveOneStepTowards(world, burrowLoc);
    }

    // ----------- LIFE -----------

    /**
     * Wakes up the rabbit and tries placing it on top of its burrow.
     * If that is not possible, places the rabbit on an adjacent empty tile.
     *
     * @param world the world in which the rabbit wakes up
     */
    @Override
    public void wakeUp(World world) {
        if (burrow == null) return;

        Location burrowLoc;
        try {
            burrowLoc = world.getLocation(burrow);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (burrowLoc == null) return;

        if (world.isTileEmpty(burrowLoc)) {
            world.setTile(burrowLoc, this);
            isSleeping = false;
            return;
        }

        Set<Location> empty = world.getEmptySurroundingTiles(burrowLoc);
        if (!empty.isEmpty()) {
            world.setTile(empty.iterator().next(), this);
            isSleeping = false;
        }
    }

    /**
     * Handles the rabbit's sleep location. If the rabbit has a burrow, it sleeps in it
     * and is removed from the world map.
     *
     * @param world the world in which the rabbit exists
     */
    @Override
    protected void handleSleepLocation(World world) {
        if (burrow != null) {
            world.remove(this);
        }
    }

    /**
     * Gets the amount of energy restored when the rabbit sleeps.
     *
     * @return the energy restored from sleeping
     */
    @Override
    protected int getSleepEnergy() { return 25; }

    // ----------- EATING -----------

    /**
     * Rabbit eats grass at the target location in the world.
     * Increases energy and removes the grass from the world.
     *
     * @param world the world in which the rabbit exists
     * @param targetLoc the location of the grass to eat
     */
    @Override
    public void eat(World world, Location targetLoc) {
        Object o = world.getNonBlocking(targetLoc);
        if (canEat(o)) {
            energy += getFoodEnergy(o);
            world.delete(o);
        }
    }

    /**
     * Check if the rabbit can eat the given object.
     *
     * @param object the object to check
     * @return true if the rabbit can eat the object, false otherwise
     */
    @Override
    protected boolean canEat(Object object) {
        return object instanceof Grass;
    }

    /**
     * Get the food energy provided by the given object.
     * In this case, eating grass provides 20 energy.
     * @param object the object to get food energy from
     * @return the food energy value
     */
    @Override
    protected int getFoodEnergy(Object object) {
        return 20;
    }

    /**
     * Get the meat value provided by the rabbit when it dies.
     *
     * @return the meat value
     */
    @Override
    protected int getMeatValue() {
        return 30;
    }

    // ----------- REPRODUCTION -----------

    /**
     * Determines the reproduction behavior for rabbits.
     * Rabbits reproduce if they are in a burrow and the burrow's capacity is not exceeded.
     *
     * @param world the world in which the rabbit exists
     */
    @Override
    public void reproduce(World world) {
        if (burrow == null) return;
        if (burrow.getRabbits().size() > 15) return;
        super.reproduce(world);
    }

    /**
     * Creates a child rabbit at the specified location in the world.
     *
     * @param world the world in which the child rabbit will be created
     * @param childLoc the location where the child rabbit will be placed
     * @return the newly created child rabbit
     */
    @Override
    protected Animal createChild(World world, Location childLoc) {
        Rabbit child = new Rabbit();
        child.setBurrow(this.burrow);
        return child;
    }

    /**
     * Determines the location for reproduction, prioritizing the burrow and its surroundings.
     *
     * @param world the world in which the rabbit exists
     * @return the location for reproduction, or null if no suitable location is found
     */
    @Override
    protected Location getReproductionLocation(World world) {
        if (burrow == null) return null;

        Location burrowLocation;
        try {
            burrowLocation = world.getLocation(burrow);
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (burrowLocation == null) return null;

        if (world.isTileEmpty(burrowLocation)) {
            return burrowLocation;
        }

        Set<Location> emptyTilesAroundBurrow = world.getEmptySurroundingTiles(burrowLocation);
        if (!emptyTilesAroundBurrow.isEmpty()) {
            return emptyTilesAroundBurrow.iterator().next();
        }

        return null;
    }


    // ----------- BURROW -----------

    /**
     * Rabbit attempts to dig a burrow at its current location in the world.
     * If the location is occupied by grass or fungi, it removes them first.
     * If the location is already occupied by a burrow, it claims that burrow.
     *
     * @param world the world in which the rabbit exists
     */
    public void digBurrow(World world) {
        Location rabbitLocation = world.getLocation(this);
        Object obj = world.getNonBlocking(rabbitLocation);

        if (obj instanceof Burrow) {
            claimBurrow(world);
            return;
        }

        if (obj != null && !(obj instanceof Grass || obj instanceof Fungi)) {
            return;
        }

        if (obj instanceof Grass || obj instanceof Fungi) {
            world.delete(obj);
        }

        Burrow newBurrow = new Burrow();
        world.setTile(rabbitLocation, newBurrow);
        setBurrow(newBurrow);

    }


    /**
     * Claims an existing burrow at or near the rabbit's current location.
     * @param world the world in which the rabbit exists
     */
    public void claimBurrow(World world) {
        Location rabbitLocation = world.getLocation(this);
        Object obj = world.getNonBlocking(rabbitLocation);
        if (obj instanceof Burrow b) {
            setBurrow(b);
            return;
        }

        Set<Location> nearby = world.getSurroundingTiles(rabbitLocation);
        for (Location loc : nearby) {
            Object obj2 = world.getNonBlocking(loc);
            if (obj2 instanceof Burrow b) {
                setBurrow(b);
                return;
            }
        }
    }

    /**
     * Returns the burrow assigned to the rabbit.
     * @return the rabbit's burrow
     */
    public Burrow getBurrow() {return burrow;}

    /**
     * Sets the burrow for the rabbit and adds the rabbit to the burrow's list.
     * @param burrow the burrow to assign
     */
    public void setBurrow(Burrow burrow) {
        this.burrow = burrow;
        this.shelter = burrow;
        if (burrow != null) {
            burrow.addRabbit(this);
        }
    }

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    /**
     * Checks if the rabbit is a child (age < 15).
     *
     * @return true if the rabbit is a child, false otherwise
     */
    @Override
    public boolean isChild() {
        return getAge() < 15;
    }

    /**
     * Provides display information for the rabbit based on its age and sleeping state.
     *
     * @return DisplayInformation object representing the rabbit's appearance
     */
    @Override
    public DisplayInformation getInformation() {
        if (isChild()) {
            if (isSleeping) {
                return new DisplayInformation(Color.WHITE, "rabbit-small-sleeping");
            } else {
                return new DisplayInformation(Color.WHITE, "rabbit-small");
            }
        } else {
            if (isSleeping) {
                return new DisplayInformation(Color.LIGHT_GRAY, "rabbit-sleeping");
            } else {
                return new DisplayInformation(Color.LIGHT_GRAY, "rabbit-large");
            }
        }
    }
}
