package Inanimate;

import Actors.Carcass;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;
import java.awt.*;
import java.util.Set;

/**
 * Fungi represents a fungal growth in the world that can infect nearby carcasses.
 * It has a limited lifespan and will spread its infection to carcasses within a certain radius.
 */
public class Fungi extends Landscape {
    private int lifespan;

    /**
     * Constructor for Fungi with a specified lifespan.
     * @param lifespan the duration the fungi will remain in the world
     */
    public Fungi(int lifespan) {
        this.lifespan = lifespan;
    }

    /**
     * Updates the fungi's state each simulation step.
     * Decreases lifespan and attempts to infect nearby carcasses.
     *
     * @param world the world in which the fungi exists
     */
    @Override
    public void act(World world) {
        lifespan--;
        if (lifespan <= 0) {
            world.delete(this);
            return;
        }

        Set<Location> tilesInInfectionRadius = getInfectionArea(world);

        for (Location tileInRadius : tilesInInfectionRadius) {
            Object o = world.getTile(tileInRadius);
            if (o instanceof Carcass carcass) {
                infectNearbyCarcass(carcass);
            }
        }
    }

    /**
     * Infects the given carcass with fungi.
     *
     * @param carcass the carcass to infect
     */
    public void infectNearbyCarcass(Carcass carcass) {
        carcass.infectWithFungi();
    }

    /**
     * Fungi do not spread on their own, so spreadChance returns 0.
     */
    @Override
    protected int spreadChance() {
        return 0;
    }

    /**
     * Fungi do not create new instances of themselves.
     */
    @Override
    public Landscape createNewInstance() {
        return null;
    }

    /**
     * Returns the set of locations within the infection radius of the fungi.
     *
     * @param world the world in which the fungi exist
     * @return set of locations within the infection radius
     */
    public Set<Location> getInfectionArea(World world) {
        int radius = 2;
        Location here = getFungiLocation(world);
        if (here == null) return Set.of();
        return world.getSurroundingTiles(here, radius);
    }

    /**
     * Returns the current location of the fungi in the world.
     *
     * @param world the world in which the fungi exist
     * @return the location of the fungi
     */
    public Location getFungiLocation(World world) {
        return world.getLocation(this);
    }

    /**
     * Returns the current lifespan of the fungi.
     *
     * @return the lifespan of the fungi
     */
    public int getLifespan() {
        return lifespan;
    }

    /**
     * Provides display information for the fungi based on its lifespan.
     *
     * @return DisplayInformation with color and icon for the fungi
     */
    @Override
    public DisplayInformation getInformation() {
        if (lifespan >= 30) {
            return new DisplayInformation(Color.ORANGE, "fungi");
        } else {
            return new DisplayInformation(Color.ORANGE, "fungi-small");
        }
    }

}
