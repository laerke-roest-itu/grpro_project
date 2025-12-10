package Inanimate;

import Actors.Wolf;

import itumulator.simulator.Actor;
import itumulator.world.World;
import itumulator.world.NonBlocking;
import java.util.ArrayList;
import java.util.List;

public class Pack implements Actor, NonBlocking{
    private List<Wolf> wolves = new ArrayList<>();
    private Den den;

    @Override
    public void act(World world) {}

    public void addWolf(Wolf wolf) {
        if (!wolves.contains(wolf)) {
            wolves.add(wolf);
            wolf.setPack(this);   // <– MEN se næste punkt
        }
    }

    public Wolf getLeader() {
        return wolves.isEmpty() ? null : wolves.getFirst();
    }

    public void claimDen(Den den) {
        this.den = den;
        // alle ulve i flokken får reference til hulen
        for (Wolf w : wolves) {
            w.setDen(den);
        }
    }

}
