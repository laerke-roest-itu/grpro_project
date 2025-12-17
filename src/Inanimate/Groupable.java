package Inanimate;

import Actors.Animal;
import itumulator.world.Location;
import java.util.List;

/**
 * An interface representing a group of animals.
 *
 * @param <T> the type of Animal that can be part of the group
 */

public interface Groupable<T extends Animal> {

    /**
     * Adds an animal to the group.
     *
     * @param animal the animal to add
     */
    void addMember(T animal);

    /**
     * Removes an animal from the group.
     *
     * @param animal the animal to remove
     */
    void removeMember(T animal);

    /**
     * Gets the leader of the group.
     *
     * @return the leader animal
     */
    T getLeader();

    /**
     * Gets the list of members in the group.
     *
     * @return the list of member animals
     */
    List<T> getMembers();

    /**
     * Gets the home location of the group.
     *
     * @return the home location
     */
    Location getHome();

    /**
     * Sets the home location of the group.
     *
     * @param location the home location to set
     */
    void setHome(Location location);
}

