package Actors;

import Inanimate.*;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

public abstract class Herbivore extends Animal {

    protected Herbivore(int maxAge) {
        super(maxAge);
    }

    // ----------- ACT -----------

    @Override
    public void act(World world) {
        // dødstjek før vi bruger world/getLocation/move osv.
        if (!isAlive) return;
        if (getAge() >= getMaxAge() || getEnergy() <= 0) {
            die(world);
            return;
        }

        // skumring: gå mod shelter
        if (world.getCurrentTime() >= World.getDayDuration() - 3) {
            seekShelter(world);
            return; // vigtigt: vi vil ikke også gå random og spise i skumring
        }

        // dag: vågn + tick + bevæg/spis
        if (world.isDay()) {
            if (world.getCurrentTime() == 0 && isSleeping) {
                wakeUp(world);
            }

            super.act(world); // Animal.act -> tickCommon (kun hvis vågen og i live)

            // hvis super.act ikke gjorde noget (fx blev sovende), stop her
            if (!isAlive || isSleeping) return;

            Location moveTo = moveRandomly(world);
            if (moveTo != null && getEnergy() < 50) {
                eat(world, moveTo);
            }
        }

        // nat: default gør Herbivore ingenting (subklasser håndterer nat)
    }

    // ----------- LIFE -----------

    @Override
    protected void handleSleepLocation(World world) {
    }

    protected boolean hasShelter() { return shelter != null; }

    // ----------- EATING -----------

    @Override
    protected boolean canEat(Object object) {
        return object instanceof Grass || object instanceof Bush;
    }

    @Override
    protected int getFoodEnergy(Object object) {
        return 0;
    }

    @Override
    protected int getMeatValue() {
        return 0;
    }

    // ----------- REPRODUCTION -----------

    @Override
    protected Animal createChild(World world, Location childLoc) {
        return null;
    }

    @Override
    protected Location getReproductionLocation(World world) {
        return null;
    }

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    @Override
    public DisplayInformation getInformation() {
        return null;
    }
}
