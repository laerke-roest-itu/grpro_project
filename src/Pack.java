import itumulator.simulator.Actor;
import itumulator.world.World;
import itumulator.world.NonBlocking;
import java.util.ArrayList;
import java.util.List;

public class Pack implements Actor, NonBlocking{
    private List<Wolf> wolves = new ArrayList<>();

    @Override
    public void act(World world) {}

    public void addWolf(Wolf wolf) {
        wolves.add(wolf);
        wolf.setPack(this);
    }

    public Wolf getLeader() {
        return wolves.isEmpty() ? null : wolves.getFirst();
    }

    public List<Wolf> getWolves() {
        return wolves;
    }

}
