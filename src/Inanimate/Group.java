package Inanimate;

import Actors.Animal;
import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.NonBlocking;
import itumulator.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Group<T extends Animal>
        implements Groupable<T>, Actor, NonBlocking {

    private final List<T> members = new ArrayList<>();
    private Location home;

    @Override
    public void addMember(T animal) {
        if (animal == null || members.contains(animal)) return;
        members.add(animal);
        animal.setGroup(this);
    }

    @Override
    public void removeMember(T animal) {
        members.remove(animal);
    }

    @Override
    public T getLeader() {
        return members.isEmpty() ? null : members.get(0);
    }

    @Override
    public List<T> getMembers() {
        return Collections.unmodifiableList(members);
    }

    @Override
    public Location getHome() {
        return home;
    }

    @Override
    public void setHome(Location location) {
        this.home = location;
    }

    @Override
    public void act(World world) {
        // Pack gør ikke noget selv (men skal eksistere i verden)
    }

}

