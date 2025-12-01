import itumulator.simulator.Actor;
import itumulator.world.NonBlocking;
import itumulator.world.World;

import java.util.ArrayList;
import java.util.List;

public class Den implements Actor, NonBlocking{
    private List<Wolf> wolves = new ArrayList<>();

    @Override
    public void act(World world) {}

    public void addWolf(Wolf wolf) {
        wolves.add(wolf);
    }

    public List<Wolf> getWolves() {
        return wolves;
    }
}
