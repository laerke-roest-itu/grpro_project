package Inanimate;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.NonBlocking;
import itumulator.world.World;
import itumulator.world.Location;

import java.util.Random;
import java.util.Set;

public abstract class Landscape implements NonBlocking, Actor, DynamicDisplayInformationProvider {
    protected Random random;
    protected Location location;
    protected abstract int spreadChance(); // implemented in Grass & Bush subclasses that return final chance value.
    protected abstract Landscape createNewInstance(); // implemented in Grass & Bush subclasses that return new instance

    public Landscape() {
        this.random = new Random();
    }

    /**
     * act method called on each tick of the simulation. Handles the spreading behavior of the landscape. Bush & Grass
     * are the subclasses that implement this method through the spreadChance and createNewInstance methods.
     *
     * @param world providing details of the position on which the actor is currently located and much more - specifics
     * readable in the World-class.
     */

    @Override
    public void act(World world) {
        spread(world);
        afterAct(world);
    }

    /**
     * Handles the spreading of the landscape to neighboring tiles based on a chance defined in subclasses.
     * @param world
     */

    public void spread(World world) { // udfør sprednings metoden med en given chance der overrides i subklasser
        Location location = world.getLocation(this);
        Set<Location> neighbours = world.getSurroundingTiles(location);

        if (random.nextInt(100) <= spreadChance()) {
            for (Location neighbour : neighbours) {
                if (!world.containsNonBlocking(neighbour)) {
                    world.setTile(neighbour, createNewInstance()); // on all neighbouring tiles that are empty
                }                                                  // a new instance of the landscape is created
            }                                                      // specified in the subclasses.
        }
    }

    /**
     * Used for further logic after act, can be overridden in subclasses. As a default, it is empty.
     * @param world
     */

    protected void afterAct(World world) {}

    /** Provides display information for the landscape. Overridden in subclasses to provide specific details.
     *
     * @return DisplayInformation object containing color and label
     */

    @Override
    public abstract DisplayInformation getInformation();
}
