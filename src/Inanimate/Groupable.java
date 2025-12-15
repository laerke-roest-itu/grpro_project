package Inanimate;

import Actors.Animal;
import itumulator.world.Location;
import java.util.List;

public interface Groupable<T extends Animal> {

    void addMember(T animal);

    void removeMember(T animal);

    T getLeader();

    List<T> getMembers();

    Location getHome();

    void setHome(Location location);
}

