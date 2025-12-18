package Actors;

import Inanimate.*;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Wolf represents a predatory animal that lives in packs.
 * It has behaviors for hunting, building dens, and reproducing.
 */
public class Wolf extends Predator {
    private Pack pack;
    private Den den;

    public Wolf(Pack pack) {
        super(240);
        this.random = new Random();
        this.shelter = null;
        this.pack = pack;
        if (pack != null) {
            pack.addMember(this);
        }
    }

    // ----------- ACT ------------

    /**
     * Wolf's act method, handling day/night behavior and life cycle.
     *
     * @param world the world in which the wolf exists
     */
    @Override
    public void act(World world) {
        if (!isAlive) return;
        super.act(world);
    }

    /**
     * Determines the wolf's behavior at night.
     * The wolf will attempt to sleep and then reproduce if a mate is present in its pack.
     *
     * @param world the world in which the wolf exists
     */
    @Override
    public void nightBehaviour(World world) {
        if (den != null) {
            sleep(world);
            List<Wolf> loveWolves = pack.getMembers();
            if ((loveWolves.size() >= 2)) {
                reproduce(world);
            }
        } else {
            energy -= 5;
        }
    }

    /**
     * Determines the wolf's behavior during the day.
     * The wolf will hunt if hungry, follow its pack leader, or move randomly.
     * If it is the leader, it may also build a den.
     *
     * @param world the world in which the wolf exists
     */
    @Override
    public void dayBehaviour(World world) {
        if (isHungry()) {
            hunt(world);
        } else if (pack != null) {
            Wolf leader = pack.getLeader();
            if (leader == this) {
                moveRandomly(world);
            } else {
                Location myLoc = world.getLocation(this);
                Location leaderLoc = null;
                if (leader != null) {
                    try {
                        leaderLoc = world.getLocation(leader);
                    } catch (IllegalArgumentException e) {}
                }

                if (myLoc != null && leaderLoc != null) {
                    int dist = distance(myLoc, leaderLoc);
                    if (dist > 1) {
                        moveOneStepTowards(world, leaderLoc);
                    } else {
                        moveRandomly(world);
                    }
                } else {
                    moveRandomly(world);
                }
            }
        } else {
            moveRandomly(world);
        }

        if (den == null && pack != null && pack.getLeader() == this) {
            buildDen(world);
        }
    }

    // ----------- LIFE -----------

    /**
     * Wolf seeks shelter in its den.
     *
     * @param world the world in which the wolf exists
     */
    @Override
    protected void handleSleepLocation(World world) {
        if (den != null) {
            world.remove(this);
        }
    }

    /**
     * Wolf wakes up from sleep, trying to stand on its den or nearby.
     *
     * @param world the world in which the wolf exists
     */
    @Override
    public void wakeUp(World world) {
        if (den == null) {
            isSleeping = false;
            return;
        }

        Location denLoc;
        try {
            denLoc = world.getLocation(den);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (denLoc == null) return;

        if (world.isTileEmpty(denLoc)) {
            world.setTile(denLoc, this);
            isSleeping = false;
            return;
        }

        Set<Location> empty = world.getEmptySurroundingTiles(denLoc);
        if (!empty.isEmpty()) {
            world.setTile(empty.iterator().next(), this);
            isSleeping = false;
        }
    }

    /**
     * Check if the wolf is a child (age < 40).
     *
     * @return true if the wolf is a child, false otherwise
     */
    @Override
    public boolean isChild() {
        return getAge() < 30;
    }

    // ----------- EATING -----------

    /**
     * Wolf eats from a carcass on the target location.
     *
     * @param world     the world in which the wolf exists
     * @param targetLoc the location of the carcass to eat from
     */
    @Override
    public void eat(World world, Location targetLoc) {
        Object o = world.getTile(targetLoc);

        if (o instanceof Carcass carcass) {
            int amount = Math.min(30, carcass.getMeatLeft());
            carcass.eaten(amount);
            energy += amount;
        }
    }

    /**
     * Check if the wolf can eat the given object.
     *
     * @param object the object to check
     * @return true if the wolf can eat the object, false otherwise
     */
    @Override
    public boolean canEat(Object object) {
        return object instanceof Carcass;
    }

    /**
     * Get the food energy provided by the given object.
     *
     * @param object the object to get food energy from
     * @return the food energy value
     */
    @Override
    protected int getFoodEnergy(Object object) {
        if (object instanceof Rabbit) return 40;
        return 0;
    }

    /**
     * Get the meat value provided by the wolf when it dies.
     *
     * @return the meat value
     */
    @Override
    protected int getMeatValue() {
        return 50;
    }

    // ----------- REPRODUCTION -----------

    /**
     * Determines the reproduction behavior for wolves.
     * Wolves only reproduce if they have a den and the pack size is not exceeded.
     *
     * @param world the world in which the wolf exists
     */
    @Override
    public void reproduce(World world) {
        if (den == null) return;
        if (pack.getMembers().size() > 10) return;
        super.reproduce(world);
    }

    /**
     * Create a child wolf in the specified location.
     *
     * @param world    the world in which the wolf exists
     * @param childLoc the location where the child wolf will be created
     * @return the newly created child wolf
     */
    @Override
    protected Animal createChild(World world, Location childLoc) {
        Wolf child = new Wolf(pack);
        child.setDen(this.den);
        return child;
    }

    /**
     * Get the reproduction location for the wolf, prioritizing its den.
     *
     * @param world the world in which the wolf exists
     * @return the location for reproduction, or null if none found
     */
    @Override
    protected Location getReproductionLocation(World world) {
        if (den == null) return null;

        Location denLocation;
        try {
            denLocation = world.getLocation(den);
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (denLocation == null) return null;

        if (world.isTileEmpty(denLocation)) return denLocation;

        Set<Location> emptyAround = world.getEmptySurroundingTiles(denLocation);
        if (!emptyAround.isEmpty()) return emptyAround.iterator().next();

        return null;
    }



    // ----------- PACK/DEN -----------

    /**
     * Simple getter for retrieving the pack.
     * @return the wolf's pack
     */
    public Pack getPack() {
        return pack;
    }

    /**
     * Builds a den for the wolf pack if the wolf is the leader.
     * @param world the world in which to build the den
     */
    public void buildDen(World world) {
        Location wolfLoc;
        try {
            wolfLoc = world.getLocation(this);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (wolfLoc == null) return;

        if (den != null) return;

        var empty = world.getEmptySurroundingTiles(wolfLoc);
        if (empty.isEmpty()) return;

        Location denLoc = null;
        for (Location loc : empty) {
            Object nb = world.getNonBlocking(loc);
            if (nb == null) {
                denLoc = loc;
                break;
            }
        }

        if (denLoc == null) return;

        den = new Den();
        world.setTile(denLoc, den);
        den.addWolf(this);

        if (pack != null && pack.getLeader() == this) {
            pack.claimDen(den);
        }
    }

    /**
     * Sets the den for the wolf.
     * @param den the den to set
     */
    public void setDen(Den den) {
        this.den = den;
        this.shelter = den;
    }

    public Den getDen() { return den; }

    /**
     * Determines the hunting area for the wolf.
     * The hunting area is a small radius around the wolf's current location.
     *
     * @param world the world in which the wolf exists
     * @return a set of locations defining the hunting area
     */
    @Override
    protected Set<Location> getHuntingArea(World world) {
        Location wolfLoc = world.getLocation(this);
        return world.getSurroundingTiles(wolfLoc, 2);
    }

    /**
     * Gets the energy cost for the wolf's movement during hunting.
     *
     * @return the energy cost as an integer
     */
    @Override
    protected int getHuntMoveCost() {
        return 8;
    }

    /**
     * Checks if the other animal is an enemy predator.
     * Wolves consider other wolves not in their pack and bears as enemies.
     *
     * @param other the other animal to check
     * @return true if the other animal is an enemy predator, false otherwise
     */
    @Override
    public boolean isEnemyPredator(Animal other) {
        if (!(other instanceof Predator)) return false;

        if (other instanceof Wolf otherWolf) {
            if (this.pack != null && this.pack == otherWolf.getPack()) {
                return false;
            }
            return true;

        } else if (other instanceof Bear) {
            return true;
        }

        return false;
    }

    /**
     * Gets the damage dealt by the wolf's attack.
     *
     * @return the attack damage value
     */
    @Override
    public int getAttackDamage() {
        return 10;
    }

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------


    /**
     * Provides display information for the wolf based on its age and sleeping state.
     *
     * @return DisplayInformation object representing the wolf's appearance
     */
    @Override
    public DisplayInformation getInformation() {
        if (isChild()) {
            if (isSleeping) {
                return new DisplayInformation(Color.LIGHT_GRAY, "wolf-small-sleeping");
            } else {
                return new DisplayInformation(Color.LIGHT_GRAY, "wolf-small");
            }
        } else {
            if (isSleeping) {
                return new DisplayInformation(Color.GRAY, "wolf-sleeping");
            } else {
                return new DisplayInformation(Color.GRAY, "wolf");
            }
        }
    }
}
