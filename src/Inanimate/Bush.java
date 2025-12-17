package Inanimate;

import itumulator.executable.DisplayInformation;
import itumulator.world.World;
import java.awt.*;
import java.util.Random;

/**
 * Represents a bush in the simulation that can grow berries and spread.
 */
public class Bush extends Landscape {

    private int berry = 0;
    private final int maxBerries = 100;
    private final int berryGrowthInterval = 10;
    private int ticksSinceLastGrowth = 0;

    /**
     * Constructor for normal use in the simulator.
     */
    public Bush() {
        this(new Random());
    }

    /**
     * Constructor for test use, allowing control over randomness.
     *
     * @param random the Random instance to use
     */
    public Bush(Random random) {
        super();
        this.random = random;
    }

    /**
     * Returns the chance of the bush spreading.
     *
     * @return the spread chance as an integer
     */
    @Override
    protected int spreadChance() { //spreadChance fra superklassen gives en værdi
        return 3;
    }

    /**
     * Creates a new instance of Bush.
     *
     * @return a new Bush instance
     */
    @Override
    protected Landscape createNewInstance() { // createNewInstance fra superklassen gives en instans af Bush
        return new Bush();
    }

    /**
     * Called after the bush acts, to handle berry growth.
     *
     * @param world the world in which the bush exists
     * afterAct overriden to handle growth on a bush
     */
    @Override
    protected void afterAct(World world) {
        ticksSinceLastGrowth++;
        if (ticksSinceLastGrowth == berryGrowthInterval) {
            ticksSinceLastGrowth = 0;
            produceBerries();
        }
    }

    /**
     * Increases the number of berries on the bush by one, up to the maximum limit.
     */
    public void produceBerries(){
        if (berry < maxBerries) {
            berry++;
        }
    }

    /**
     * Checks if the bush has any berries.
     *
     * @return true if the bush has one or more berries, false otherwise
     */
    public boolean hasBerries() {
        if (berry > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the current number of berries on the bush.
     *
     * @return the number of berries
     */
    public int getBerryCount(){
        return berry;
    }

    /**
     * Resets the number of berries on the bush to zero, indicating they have been eaten.
     */
    public void berriesEaten() {
        berry = 0;
    }

    /**
     * Provides display information for the bush based on its berry status.
     *
     * @return DisplayInformation object representing the bush's appearance
     */
    @Override
    public DisplayInformation getInformation() {
        if (hasBerries()) {
            return new DisplayInformation(Color.GREEN, "bush-berries");
        } else {
            return new DisplayInformation(Color.GREEN, "bush");
        }
    }




}
