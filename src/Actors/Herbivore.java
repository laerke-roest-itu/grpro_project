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
        if (!isAlive) return;

        if (getAge() >= getMaxAge() || getEnergy() <= 0) {
            die(world);
            return;
        }

        // skumring: gå mod shelter (home/burrow/etc.)
        if (world.getCurrentTime() >= World.getDayDuration() - 3) {
            seekShelter(world);
            return;
        }

        // nat: subklasse bestemmer
        if (world.isNight()) {
            nightBehaviour(world);
            return;
        }

        // dag
        if (world.isDay()) {
            if (world.getCurrentTime() == 0 && isSleeping) wakeUp(world);

            super.act(world); // tickCommon (kun hvis vågen)
            if (!isAlive || isSleeping) return;

            dayBehaviour(world); // subklasse kan ændre bevægelse/spis
        }
    }

    // ----------- LIFE -----------

    @Override
    protected void handleSleepLocation(World world) {
    }

    /** Default dag: gå random og spis hvis sulten */
    protected void dayBehaviour(World world) {
        Location moveTo = moveRandomly(world);
        if (moveTo != null && isHungry()) {
            eat(world, moveTo);
        }
    }

    /** Default nat: sov hvis man har shelter, ellers mist energi */
    protected void nightBehaviour(World world) {
        if (hasShelter()) sleep(world);
        else energy -= 5;
    }

    protected boolean hasShelter() { return shelter != null; }

    // ----------- EATING -----------

    @Override
    protected boolean canEat(Object object) {
        if (object instanceof Grass) return true;
        if (object instanceof Bush bush) return bush.hasBerries();
        return false;
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
