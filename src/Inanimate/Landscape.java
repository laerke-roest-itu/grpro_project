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
    protected abstract int spreadChance();
    protected abstract Landscape createNewInstance();



    public Landscape() {
        this.random = new Random();
    }


    @Override
    public final void act(World world) {
        doSpread(world);
        afterAct(world);
    }

    private void doSpread(World world) { // udfør sorednings methoden med en given chance der overrides i subklasser
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



    protected void afterAct(World world) {}

    @Override
    public abstract DisplayInformation getInformation();
}
