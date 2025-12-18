package Inanimate;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.NonBlocking;
import itumulator.world.World;
import itumulator.world.Location;

import java.util.Random;
import java.util.Set;

/**
 * An abstract class representing non-blocking landscape elements in the world.
 * Landscape elements can act and spread to neighboring tiles.
 */
public abstract class Landscape implements NonBlocking, Actor, DynamicDisplayInformationProvider {
    protected Random random;
    protected Location location;

    /**
     * Default constructor initializing the random number generator.
     */
    public Landscape() {
        this.random = new Random();
    }

    /**
     * Updates the landscape's state each simulation step.
     * Handles the spreading behavior to adjacent tiles.
     *
     * @param world the world in which the landscape exists
     */
    @Override
    public void act(World world) {
        spread(world);
    }

    /**
     * Handles the spreading of the landscape to neighboring tiles based on a chance defined in subclasses.
     *
     * @param world the world in which the landscape spreads
     */
    public void spread(World world) {
        Location location = world.getLocation(this);
        Set<Location> neighbours = world.getSurroundingTiles(location);

        if (random.nextInt(100) <= spreadChance()) {
            for (Location neighbour : neighbours) {
                if (!world.containsNonBlocking(neighbour)) {
                    world.setTile(neighbour, createNewInstance());
                }
            }
        }
    }

    /**
     * Returns the chance of the landscape element spreading to adjacent tiles.
     *
     * @return the spread chance as an integer percentage (0-100)
     */
    protected abstract int spreadChance();

    /**
     * Creates a new instance of the specific landscape element.
     *
     * @return a new Landscape instance
     */
    protected abstract Landscape createNewInstance();

    /**
     * Provides display information for the landscape.
     * Overridden in subclasses to provide specific details.
     *
     * @return DisplayInformation object containing color and label
     */
    @Override
    public abstract DisplayInformation getInformation();
}
