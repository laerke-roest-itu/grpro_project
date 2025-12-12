package Inanimate;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.NonBlocking;
import itumulator.world.World;
import itumulator.world.Location;

import java.util.Random;

public abstract class Landscape implements NonBlocking, Actor, DynamicDisplayInformationProvider {
    protected Random random;
    protected Location location;

    public Landscape() {
        this.random = new Random();
    }

    @Override
    public void act(World world) {
        // Default implementation
    }

    public void setLocation(Location loc) {
        this.location = loc;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public abstract DisplayInformation getInformation();
}
