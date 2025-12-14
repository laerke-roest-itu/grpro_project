package Actors;

import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.Set;

public class Deer extends Herbivore {
    protected boolean isFleeing;
    private DeerPack pack;


    public Deer(DeerPack pack) {
        super(300);
        this.pack = pack;
        this.isFleeing = false;
        if (pack != null) pack.add(this);
    }


    @Override
    public void act(World world) {
        Location myLoc = world.getLocation(this);
        if (myLoc == null) return;

        Location predatorLoc = findNearbyPredator(world, myLoc, 2);

        if (predatorLoc != null) {
            isFleeing = true;
            Flee(world, myLoc, predatorLoc);
            alertNearbyDeer(world, myLoc, predatorLoc);
            return;
        }

        isFleeing = false;
        super.act(world); // kun én gang
    }

    private Location findNearbyPredator(World world, Location myLoc, int radius) {
        for (Location loc : world.getSurroundingTiles(myLoc, radius)) {
            Object o = world.getTile(loc);
            if (o instanceof Predator) return loc; // evt. vælg den nærmeste
        }
        return null;
    }


    public void Flee(World world, Location myLoc, Location predatorLoc) {
        Set<Location> emptyTiles = world.getEmptySurroundingTiles(myLoc);
        if (!emptyTiles.isEmpty()) {
            Location bestLoc = null;
            int bestDistance = -1;

            // vælg den tomme tile der ligger længst væk fra predator
            for (Location candidate : emptyTiles) {
                int dist = distance(candidate, predatorLoc);
                if (dist > bestDistance) {
                    bestDistance = dist;
                    bestLoc = candidate;
                }
            }

            if (bestLoc != null) {
                world.move(this, bestLoc);
            }
        }
    }

    private void alertNearbyDeer(World world, Location myLoc, Location predLoc) {
        Set<Location> nearbyTiles = world.getSurroundingTiles(myLoc, 2); // fx radius 3
        for (Location loc : nearbyTiles) {
            Object actor = world.getTile(loc);
            if (actor instanceof Deer otherDeer && otherDeer != this) {
                otherDeer.isFleeing = true;
                Location otherLoc = world.getLocation(otherDeer);
                if (otherLoc != null) {
                    otherDeer.Flee(world, otherLoc, predLoc);
                }
            }
        }
    }

    @Override
    public DisplayInformation getInformation() {
        if (isChild()) {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "deer-small-sleeping");
            } else if (isFleeing) {
                return new DisplayInformation(Color.LIGHT_GRAY, "deer-small-afraid");
            } else {
                return new DisplayInformation(Color.GRAY, "deer-small"); // billede af hjorteunge
            }
        } else {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "deer-sleeping");
            } else if (isFleeing) {
                return new DisplayInformation(Color.LIGHT_GRAY, "deer-afraid");
            } else {
                return new DisplayInformation(Color.DARK_GRAY, "deer"); // billede af voksen hjort
            }
        }
    }
}
