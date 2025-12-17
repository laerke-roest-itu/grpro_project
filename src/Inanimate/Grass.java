package Inanimate;

import itumulator.executable.DisplayInformation;

import java.awt.*;
import java.util.Random;

public class Grass extends Landscape {
    //private final Random random;

    // normal brug i simulatoren
    public Grass() {
        this(new Random());
    }

    // test-brug: styr tilfældighed
    public Grass(Random random) {
        super();
        this.random = random;
    }

    @Override
    protected int spreadChance() {
        return 5;
    } // spreadChance fra superklassen gives en værdi

    @Override
    protected Landscape createNewInstance() {
        return new Grass();
    } // createNewInstance fra superklassen gives en instans af Grass


    @Override
    public DisplayInformation getInformation() {
        return new DisplayInformation(Color.GREEN, "grass");
    }
}
