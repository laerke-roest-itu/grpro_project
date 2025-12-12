package Inanimate;

import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.NonBlocking;

import java.util.Random;

public abstract class Landscape implements NonBlocking, Actor, DynamicDisplayInformationProvider {
    protected Random random;

    public Landscape() {
        this.random = new Random();
    }
}
