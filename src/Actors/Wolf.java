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
    private Pack pack;       // reference to the wolf's pack
    private Den den;        // the Wolf's den

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

    @Override
    public void nightBehaviour(World world) {
        if (den != null) {
            List<Wolf> loveWolves = pack.getMembers();
            if ((loveWolves.size() >= 2)) {
                reproduce(world);
            }
            sleep(world);
        } else {
            energy -= 5; // lose energy from exposure if no Den
        }
    }

    @Override
    public void dayBehaviour(World world) {
        if (isHungry()) {
            hunt(world);
        } else if (pack.getLeader() == this) {
            moveRandomly(world);
        } else {
            seekPack(world);
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
            world.remove(this); // the wolf sleeps in its den
        }
    }

    /**
     * Wolf wakes up from sleep, trying to stand on its den or nearby.
     *
     * @param world the world in which the wolf exists
     */

    @Override
    public void wakeUp(World world) {
        // If there is no Den, Wolf cannot wake up there
        if (den == null) {
            isSleeping = false;
            return;
        }

        Location denLoc;
        try {
            denLoc = world.getLocation(den);
        } catch (IllegalArgumentException e) {
            // den findes ikke på kortet længere
            return;
        }
        if (denLoc == null) return;

        // 1) try and stand on the den tile if it's empty
        if (world.isTileEmpty(denLoc)) {
            world.setTile(denLoc, this);
            isSleeping = false;
            return;
        }

        // 2) if not try an empty neighbour tile
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
        // hvis det ikke er Carcass → gør ingenting
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

   /* @Override
    protected boolean isHungry() {
        return getEnergy() < 75;
    }
*/
    // ----------- REPRODUCTION -----------

    @Override
    public void reproduce(World world) {
        if (den == null) return;
        if (pack.getMembers().size() > 10) return;
        super.reproduce(world);
    }

    /** Create a child wolf in the specified location.
     *
     * @param world the world in which the wolf exists
     * @param childLoc the location where the child wolf will be created
     * @return the newly created child wolf
     */

    @Override
    protected Animal createChild(World world, Location childLoc) {
        Wolf child = new Wolf(pack);
        child.setDen(this.den);
        return child;
    }

    /** Get the reproduction location for the wolf, prioritizing its den.
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
     * Wolf behaviour to seek it's pack on account of its leader and what to do if there is none in the moment
     * the wolf checks for a leader.
     * @param world
     */
    private void seekPack(World world) {
        if (pack == null) return;

        Wolf leader = pack.getLeader();
        if (leader == null || leader == this) return;

        Location leaderLoc;
        try {
            leaderLoc = world.getLocation(leader);
        } catch (IllegalArgumentException e) {
            return; // the leader does not exist in the World right now
        }

        if (leaderLoc == null) return;

        moveOneStepTowards(world, leaderLoc); // choose energy price
    }

    /**
     * Simple getter for retrieving the pack - used in later logic  @isEnemyPredator.
     * @return
     */

    public Pack getPack() {
        return pack;
    }

    /**
     * Den building method, the wolf finds a location based upon its own location and creates a Den on location
     * depending if the location is void of a used tile.
     * @param world
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

        // find en nabo-tile der er tom og uden non-blocking
        var empty = world.getEmptySurroundingTiles(wolfLoc);
        if (empty.isEmpty()) return;

        Location denLoc = null;
        for (Location loc : empty) {
            // tjek om der er non-blocking på tile
            Object nb = world.getNonBlocking(loc);
            if (nb == null) {
                denLoc = loc;
                break;
            }
        }

        if (denLoc == null) return; // ingen egnet tile fundet

        den = new Den();
        world.setTile(denLoc, den);
        den.addWolf(this);

        if (pack != null && pack.getLeader() == this) {
            pack.claimDen(den);
        }
    }

    /**
     * Set den for use in Pack-class.
     * @param den
     */

    public void setDen(Den den) {
        this.den = den;
        this.shelter = den;
    }



    @Override
    protected Set<Location> getHuntingArea(World world) {
        Location wolfLoc = world.getLocation(this);
        // fx radius 2 som før
        return world.getSurroundingTiles(wolfLoc, 2);
    }

    @Override
    protected int getHuntMoveCost() {
        return 8;   // same as before
    }

    /**
     * Method to check if there is an enemy predator, if it's part of Wolf's own Pack, do not consider Wolf an enemy
     * otherwise, consider enemy. If a bear, always consider an enemy.
     * @param other the other animal to check
     * @return
     */

    @Override
    public boolean isEnemyPredator(Animal other) {
        if (!(other instanceof Predator)) return false;

        // If there's a wolf
        if (other instanceof Wolf otherWolf) {
            // same pack → not an enemy
            if (this.pack != null && this.pack == otherWolf.getPack()) {
                return false;
            }
            // otherwise → enemy
            return true;

        } else if (other instanceof Bear) {
            return true;
        }

        return false;
    }

    /**
     * Method of taking damage from an attack.
     * @return int value of damage to energy.
     */

    @Override
    public int getAttackDamage() {
        return 10;
    }

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    public Den getDen() { return den; }


    /**
     * Display information method for Wolf with colour and label (imageKey .png file).
     * @return dependant states of imagefile depending on state of Wolf regarding age and state.
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
