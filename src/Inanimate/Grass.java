package Inanimate;

import itumulator.executable.DisplayInformation;

import java.awt.*;
import java.util.Random;

public class Grass extends Landscape {

    /**
     * Constructor for normal use in the simulator.
     */
    public Grass() {
        this(new Random());
    }

    /**
     * Constructor for test use, allowing control over randomness.
     *
     * @param random the Random instance to use
     */
    public Grass(Random random) {
        super();
        this.random = random;
    }

    /**
     * Returns the chance of the grass spreading.
     *
     * @return the spread chance as an integer
     */
    @Override
    protected int spreadChance() {
        return 5;
    }

    /**
     * Creates a new instance of Grass.
     *
     * @return a new Grass instance
     */
    @Override
    protected Landscape createNewInstance() {
        return new Grass();
    }

    /**
     * Provides display information for the grass.
     *
     * @return DisplayInformation object containing color and label
     */
    @Override
    public DisplayInformation getInformation() {
        return new DisplayInformation(Color.GREEN, "grass");
    }
}
