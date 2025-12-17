package Actors;

import Inanimate.*;
import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.Random;

/**
 * Carcass represents the remains of a dead animal in the simulation.
 * It contains meat that can be eaten by other actors and can rot over time.
 * If it has fungi, it will rot faster and may spawn fungi when it decays completely.
 */
public class Carcass implements Actor, DynamicDisplayInformationProvider {
    private int meatLeft;
    private int maxMeat;
    private int rotTimer;
    private boolean hasFungi;
    private Random random;
    private Fungi fungi; // til test

    /**
     * Main constructor allowing control over all parameters.
     *
     * @param meatLeft  the amount of meat left on the carcass
     * @param rotTimer  the time until the carcass rots completely
     * @param hasFungi  whether the carcass is infected with fungi
     * @param random    the Random instance to use for randomness
     */
    public Carcass(int meatLeft, int rotTimer, boolean hasFungi, Random random) {
        this.meatLeft = meatLeft;
        this.rotTimer = rotTimer;
        maxMeat = meatLeft;
        this.hasFungi = hasFungi;
        this.random = random;
    }

    /**  * Constructor for normal use without fungi.
     *
     * @param meatLeft the amount of meat left on the carcass
     * @param rotTimer the time until the carcass rots completely
     */
    public Carcass(int meatLeft, int rotTimer) {
        this(meatLeft, rotTimer, false, new Random());
    }

    /**  * Constructor for normal use with option for fungi.
     *
     * @param meatLeft the amount of meat left on the carcass
     * @param rotTimer the time until the carcass rots completely
     * @param hasFungi whether the carcass is infected with fungi
     */
    public Carcass(int meatLeft, int rotTimer, boolean hasFungi) {
        this(meatLeft, rotTimer, hasFungi, new Random());
    }

    /**  * Constructor for test use, allowing control over randomness.
     *
     * @param random
     */
    public Carcass(Random random) {
        this(10, 10, false, random); // testværdier
    }

    /**
     * The act method is called each tick to update the state of the carcass.
     * It decreases the rot timer, attempts to spawn fungi, and handles decay.
     *
     * @param world the world in which the carcass exists
     */
    @Override
    public void act(World world) {
        if (hasFungi) {
            rotTimer--;
        }
        rotTimer--;
        trySpawnFungi();

        if (rotTimer <= 0 || meatLeft <= 0) {
            Location myLoc = world.getLocation(this);

            boolean spawnFungi = hasFungi;

            world.delete(this);

            if (!spawnFungi /*|| myLoc == null*/) {
                return;
            }

            Object nb = world.getNonBlocking(myLoc);

            if (nb == null) {
                world.setTile(myLoc, new Fungi(calculateFungiLifespan()));

            } else if (nb instanceof Grass) {
                world.delete(nb);
                world.setTile(myLoc, new Fungi(calculateFungiLifespan()));

            } else {
                // cannot spawn fungi on Bush, Burrow or Den
            }
        }
    }

    /**  * Attempts to spawn fungi on the carcass with a certain probability.
     */
    public void trySpawnFungi() {
        if (hasFungi) return;

        double chance = 0.05;

        if (random.nextDouble() < chance) {
            hasFungi = true;
        }
    }

    /**  * Infects the carcass with fungi if it is not already infected.
     */
    public void infectWithFungi() {
        if (hasFungi) {
            return;
        }
        hasFungi = true;
    }

    /**  * Reduces the amount of meat left on the carcass by the specified amount.
     *
     * @param amount the amount of meat to be eaten
     */
    public void eaten(int amount) {
        meatLeft -= amount;
        if (meatLeft <= 0) {
            meatLeft = 0;
        }
    }

    /**  * Gets the amount of meat left on the carcass.
     *
     * @return the amount of meat left
     */
    public int getMeatLeft() {
        return meatLeft;
    }

    /**  * Calculates the lifespan of fungi spawned from this carcass.
     *
     * @return the lifespan of the fungi
     */
    public int calculateFungiLifespan() {
        return maxMeat * 2;
    }

    /**  * Provides display information for the carcass based on the amount of meat left.
     *
     * @return the display information for the carcass
     */
    @Override
    public DisplayInformation getInformation() {
        if (getMeatLeft() >= 50) {
            return new DisplayInformation(Color.BLACK, "carcass");
        } else if (getMeatLeft() >= 0 && getMeatLeft() < 50) {
            return new DisplayInformation(Color.BLACK, "carcass-small");
        } else {
            return null;
        }
    }
}

