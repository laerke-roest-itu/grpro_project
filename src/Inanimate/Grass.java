package Inanimate;

import itumulator.executable.DisplayInformation;

import java.awt.*;

public class Grass extends Landscape {

    @Override
    protected int spreadChance() {
        return 5;
    }

    @Override
    protected Landscape createNewInstance() {
        return new Grass();
    }



    @Override
    public DisplayInformation getInformation() {
        return new DisplayInformation(Color.GREEN, "grass");
    }
}
