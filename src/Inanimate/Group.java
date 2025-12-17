package Inanimate;

import Actors.Animal;
import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.NonBlocking;
import itumulator.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An abstract class representing a group of animals.
 *
 * @param <T> the type of Animal that can be part of the group
 */
public abstract class Group<T extends Animal>
        implements Groupable<T>, Actor, NonBlocking {

    private final List<T> members = new ArrayList<>();
    private Location home;

    /**
     * Adds an animal to the group.
     *
     * @param animal the animal to add
     */
    @Override
    public void addMember(T animal) {
        if (animal == null || members.contains(animal)) return;
        members.add(animal);
        animal.setGroup(this);
    }

    /**
     * Removes an animal from the group.
     *
     * @param animal the animal to remove
     */
    @Override
    public void removeMember(T animal) {
        members.remove(animal);
    }

    /**
     * Gets the leader of the group.
     *
     * @return the leader animal
     */
    @Override
    public T getLeader() {
        return members.isEmpty() ? null : members.get(0);
    }

    /**
     * Gets the list of members in the group.
     *
     * @return the list of member animals
     */
    @Override
    public List<T> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /**
     * Gets the home location of the group.
     *
     * @return the home location
     */
    @Override
    public Location getHome() {
        return home;
    }

    /**
     * Sets the home location of the group.
     *
     * @param location the home location to set
     */
    @Override
    public void setHome(Location location) {
        this.home = location;
    }

    /**
     * The act method for the group. Currently, the group does not perform any actions.
     *
     * @param world the world in which the group exists
     */
    @Override
    public void act(World world) {
        // Pack gør ikke noget selv (men skal eksistere i verden)
    }

}

