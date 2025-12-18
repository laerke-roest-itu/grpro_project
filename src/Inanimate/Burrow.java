package Inanimate;

import Actors.Rabbit;
import itumulator.executable.DisplayInformation;
import itumulator.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Inanimate.Burrow represents a rabbit hole in the world.
 * It can be placed via input files or dug by rabbits.
 * Other actors can stand on a burrow without affecting it.
 */
public class Burrow extends Landscape {
    private List<Rabbit> rabbits = new ArrayList<>();

    /**
     * Burrows do not spread, so spreadChance returns 0.
     */
    @Override
    protected int spreadChance() {
        return 0;
    }

    /**
     * Burrows do not create new instances of themselves.
     */
    @Override
    protected Landscape createNewInstance() {
        return null;
    }

    /**
     * Updates the burrow's state each simulation step.
     * Burrows are passive and do not perform any actions.
     *
     * @param world the world in which the burrow exists
     */
    @Override
    public void act(World world) {
    }

    /**
     * Adds a rabbit to the burrow's list of residents.
     *
     * @param rabbit the rabbit to add
     */
    public void addRabbit(Rabbit rabbit) {
        rabbits.add(rabbit);
    }

    /**
     * Returns the list of rabbits residing in the burrow.
     *
     * @return List of rabbits in the burrow
     */
    public List<Rabbit> getRabbits() {
        return rabbits;
    }

    /**
     * Provides display information for the burrow.
     *
     * @return DisplayInformation with color and icon for the burrow
     */
    @Override
    public DisplayInformation getInformation() {
        return new DisplayInformation(Color.DARK_GRAY, "hole-small");
    }
}
