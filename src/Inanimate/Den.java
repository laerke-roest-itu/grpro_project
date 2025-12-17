package Inanimate;

import Actors.Wolf;
import itumulator.executable.DisplayInformation;
import itumulator.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Den represents a wolf den in the world.
 * It can house multiple wolves.
 */
public class Den extends Landscape {
    private List<Wolf> wolves = new ArrayList<>();

    /**
     * Dens do not spread, so spreadChance returns 0.
     */
    @Override
    protected int spreadChance() {
        return 0;
    }

    /**
     * Dens do not create new instances of themselves.
     */
    @Override
    protected Landscape createNewInstance() {
        return null;
    }

    /**
     * Dens do not perform any actions themselves.
     * Wolves may interact with them, but the den is passive.
     */
    @Override
    public void act(World world) {}

    /**
     * Adds a wolf to the den's list of residents.
     *
     * @param wolf the wolf to add
     */
    public void addWolf(Wolf wolf) {
        wolves.add(wolf);
    }

    /**
     * Returns the list of wolves residing in the den.
     *
     * @return List of wolves in the den
     */
    public List<Wolf> getWolves() {
        return wolves;
    }

    /**
     * Provides display information for the den.
     *
     * @return DisplayInformation object with color and label
     */
    @Override
    public DisplayInformation getInformation() {
        return new DisplayInformation(Color.DARK_GRAY, "hole");
    }
}
