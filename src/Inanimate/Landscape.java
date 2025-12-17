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
    protected abstract int spreadChance(); // implementeret i Grass & Bush subklasserne der giver endelig værdi
    protected abstract Landscape createNewInstance(); //implementeret i Grass & Bush for at bestemme hvilken instans
                                                      //der skabes.



    public Landscape() {
        this.random = new Random();
    }


    @Override
    public void act(World world) {
        doSpread(world);
        afterAct(world);
    }

    private void doSpread(World world) { // udfør sprednings methoden med en given chance der overrides i subklasser
        Location location = world.getLocation(this);
        Set<Location> neighbours = world.getSurroundingTiles(location);

        if (random.nextInt(100) <= spreadChance()) {
            for (Location neighbour : neighbours) {
                if (!world.containsNonBlocking(neighbour)) {
                    world.setTile(neighbour, createNewInstance()); // på alle nabofelter der ikke allerede er optaget
                }                                                  // sættes en ny instans af den givne Landscape subklasse
            }
        }
    }

    protected void afterAct(World world) {} // anvendes til yderligere logik efter act, kan overrides i subklasser
                                            // som default tom
    @Override
    public abstract DisplayInformation getInformation();
}
