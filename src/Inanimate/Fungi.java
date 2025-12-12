package Inanimate;

import Actors.Carcass;
import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.NonBlocking;
import itumulator.world.World;
import java.util.Random;

import java.awt.*;
import java.util.Set;

public class Fungi extends Landscape {
    private int lifespan;

    public Fungi(int lifespan) {
        this.lifespan = lifespan;
    }

    @Override
    public void act(World world) {
        lifespan--;
        if (lifespan <= 0) {
            world.delete(this); // svampen dør
            return;
        }

        Set<Location> tilesInInfectionRadius = getInfectionArea(world);

        for (Location tileInRadius : tilesInInfectionRadius) {
            Object o = world.getTile(tileInRadius);
            if (o instanceof Carcass carcass) {
                infectNearbyCarcass(tileInRadius, world, carcass);
            }
        }
    }

    public void infectNearbyCarcass(Location loc, World world, Carcass carcass) {
        carcass.infectWithFungi();
    }

    public Set<Location> getInfectionArea(World world) {
        int radius = 2;
        Location here = getFungiLocation(world);
        if (here == null) return Set.of();
        return world.getSurroundingTiles(here, radius);
    }

    public Location getFungiLocation(World world) {
        return world.getLocation(this);
    }

    public int getLifespan() {
        return lifespan;
    }

    @Override
    public DisplayInformation getInformation() {
        if (lifespan >= 30) {
            return new DisplayInformation(Color.DARK_GRAY, "fungi");
        } else {
            return new DisplayInformation(Color.LIGHT_GRAY, "fungi-small");
        }
    }

}
