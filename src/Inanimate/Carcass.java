package Inanimate;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.World;

import java.awt.*;

public class Carcass implements Actor, DynamicDisplayInformationProvider {
    private int meatLeft;      // hvor meget kød der er tilbage
    private int rotTimer;      // hvor mange ticks før ådslet rådner væk// hvilken art det stammer fra

    public Carcass(int meatLeft, int rotTimer) {
        this.meatLeft = meatLeft;
        this.rotTimer = rotTimer;
    }

    @Override
    public void act(World world) {
        rotTimer--;
        if (rotTimer <= 0 || meatLeft <= 0) {
            world.delete(this); // ådslet forsvinder
        }
    }

    public void eaten(int amount) {
        meatLeft -= amount;
        if (meatLeft <= 0) {
            meatLeft = 0;
        }
    }

    public int getMeatLeft() {
        return meatLeft;
    }

    @Override
    public DisplayInformation getInformation() {
        if (meatLeft >= 50) {
            return new DisplayInformation(Color.GRAY, "carcass");
        } else if (meatLeft >= 0 && meatLeft < 50) {
            return new DisplayInformation(Color.GRAY, "carcass-small");
        } else {
            return null;
        }
    }
}

