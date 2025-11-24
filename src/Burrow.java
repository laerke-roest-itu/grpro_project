import itumulator.simulator.Actor;
import itumulator.world.World;
import itumulator.world.NonBlocking;

/**
 * Burrow represents a rabbit hole in the world.
 * It can be placed via input files or dug by rabbits.
 * Other actors can stand on a burrow without affecting it.
 */
public class Burrow implements Actor, NonBlocking {

    @Override
    public void act(World world) {
        // Burrows do not perform any actions themselves.
        // Rabbits may interact with them, but the burrow is passive.
    }

    @Override
    public String toString() {
        return "Burrow";
    }
}
