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
     * Burrows do not perform any actions themselves.
     * Rabbits may interact with them, but the burrow is passive.
     */
    @Override
    public void act(World world) {
        // Burrows do not perform any actions themselves.
        // Rabbits may interact with them, but the burrow is passive.
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
