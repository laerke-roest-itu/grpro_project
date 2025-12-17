package Inanimate;

import Actors.Wolf;
import itumulator.executable.DisplayInformation;
import itumulator.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Den extends Landscape {
    private List<Wolf> wolves = new ArrayList<>();

    @Override
    protected int spreadChance() {
        return 0;
    }

    @Override
    protected Landscape createNewInstance() {
        return new Den();
    }


    @Override
    public void act(World world) {}

    public void addWolf(Wolf wolf) {
        wolves.add(wolf);
    }

    public List<Wolf> getWolves() {
        return wolves;
    }

    @Override
    public DisplayInformation getInformation() {
        return new DisplayInformation(Color.GREEN, "hole");
    }
}
