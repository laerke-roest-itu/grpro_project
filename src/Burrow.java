import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.World;
import itumulator.world.NonBlocking;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Burrow represents a rabbit hole in the world.
 * It can be placed via input files or dug by rabbits.
 * Other actors can stand on a burrow without affecting it.
 */
public class Burrow implements Actor, NonBlocking, DynamicDisplayInformationProvider {
    private List<Rabbit> rabbits = new ArrayList<>();

    @Override
    public void act(World world) {
        // Burrows do not perform any actions themselves.
        // Rabbits may interact with them, but the burrow is passive.
    }

    public void addRabbit(Rabbit rabbit) {
        rabbits.add(rabbit);
    }

    public List<Rabbit> getRabbits() {
        return rabbits;
    }
    @Override
    public DisplayInformation getInformation() {
        return new DisplayInformation(Color.ORANGE, "hole-small");
    }
}
