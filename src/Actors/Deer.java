package Actors;

import Inanimate.Herd;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.Set;

/**
 * Deer represents a herbivorous animal that lives in herds.
 * It has behaviors for fleeing from predators and staying with its herd.
 */
public class Deer extends Herbivore {
    private Herd herd;
    private boolean isFleeing;

    /**
     * Constructor for Deer.
     *
     * @param herd the herd to which the deer belongs
     */
    public Deer(Herd herd) {
        super(300);
        this.herd = herd;
        this.isFleeing = false;

        if (herd != null) {
            herd.addMember(this);
        }
    }

    /**
     * Seek shelter at the herd's home location.
     *
     * @param world the world in which the deer exists
     */
    @Override
    public void seekShelter(World world) {
        if (herd == null) return;
        Location home = herd.getHome();
        if (home == null) return;
        moveOneStepTowards(world, home);
    }

    // ----------- ACT -----------

    /**
     * Deer act: flee from predators, else normal herbivore behavior
     *
     * @param world the world in which the deer acts
     */
    @Override
    public void act(World world) {
        if (!isAlive) return;

        if (getAge() >= getMaxAge() || getEnergy() <= 0) {
            die(world);
            return;
        }

        tickCommon();

        if (world.isDay() && isSleeping) {
            wakeUp(world);
        }

        if (isSleeping) return;

        Location myLoc;
        try {
            myLoc = world.getLocation(this);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (myLoc == null) return;

        if (world.getCurrentTime() >= World.getTotalDayDuration() - 3) {
            seekShelter(world);
        }

        if (herd != null && herd.getHome() == null && herd.getLeader() == this) {
            herd.setHome(myLoc);
        }

        Location predatorLoc = findNearbyPredator(world, myLoc, 2);
        if (predatorLoc != null) {
            isFleeing = true;
            flee(world, myLoc, predatorLoc);
            alertNearbyDeer(world, myLoc, predatorLoc, 2);
            return;
        }

        isFleeing = false;

        if (world.isNight()) {
            nightBehaviour(world);
            return;
        }

        if (world.isDay()) {
            dayBehaviour(world);
        }
    }

    /**
     * Determines the deer's behavior during the day.
     * The deer will follow its herd leader or move randomly and eat.
     *
     * @param world the world in which the deer exists
     */
    @Override
    public void dayBehaviour(World world) {
        if (herd != null) {
            Deer leader = herd.getLeader();

            if (leader == this || this.isHungry()) {
                super.dayBehaviour(world);
            } else {
                if (leader != null) {
                    Location myLoc;
                    Location leaderLoc;

                    try {
                        myLoc = world.getLocation(this);
                        leaderLoc = world.getLocation(leader);
                    } catch (IllegalArgumentException e) {
                        super.dayBehaviour(world);
                        return;
                    }

                    if (myLoc != null && leaderLoc != null) {
                        int dist = distance(myLoc, leaderLoc);

                        if (dist > 2) {
                            moveOneStepTowards(world, leaderLoc);
                        } else {
                            super.dayBehaviour(world);
                        }
                    } else {
                        super.dayBehaviour(world);
                    }
                } else {
                    super.dayBehaviour(world);
                }
            }
        } else {
            super.dayBehaviour(world);
        }
    }

    /**
     * Determines the deer's behavior at night.
     * The deer will sleep and may reproduce if part of a herd.
     *
     * @param world the world in which the deer exists
     */
    @Override
    public void nightBehaviour(World world) {
        sleep(world);
        if (herd != null && herd.getMembers().size() >= 2) {
            reproduce(world);
        }
    }

    /**
     * Determines the reproduction behavior for deer.
     * Deer only reproduce if the herd size is not exceeded.
     *
     * @param world the world in which the deer exists
     */
    @Override
    public void reproduce(World world) {
        if (herd != null && herd.getMembers().size() >= 10) return;

        super.reproduce(world);
    }

    @Override
    protected Animal createChild(World world, Location childLoc) {
        return new Deer(herd);
    }

    @Override
    protected Location getReproductionLocation(World world) {
        Location myLoc = world.getLocation(this);
        if (myLoc == null) return null;

        Set<Location> emptyAround = world.getEmptySurroundingTiles(myLoc);
        if (!emptyAround.isEmpty()) return emptyAround.iterator().next();

        return null;
    }

    /**
     * Checks if the deer is a child (age < 30).
     *
     * @return true if the deer is a child, false otherwise
     */
    @Override
    public boolean isChild() {
        return getAge() < 30;
    }

    /**
     * Handles the deer's sleep location. Deer do not use specific shelters like burrows.
     *
     * @param world the world in which the deer exists
     */
    @Override
    protected void handleSleepLocation(World world) {
    }

    // ----------- FLEEING -----------

    /**
     * Find nearby predator within a given radius.
     *
     * @param world the world to search in
     * @param from the location to search from
     * @param radius the search radius
     * @return the location of a nearby predator, or null if none found
     */
    private Location findNearbyPredator(World world, Location from, int radius) {
        for (Location loc : world.getSurroundingTiles(from, radius)) {
            Object o = world.getTile(loc);
            if (o instanceof Predator) {
                return loc;
            }
        }
        return null;
    }

    /**
     * Flee from a predator by moving to the farthest empty tile.
     *
     * @param world the world in which the deer exists
     * @param myLoc the current location of the deer
     * @param predatorLoc the location of the predator
     */
    private void flee(World world, Location myLoc, Location predatorLoc) {
        Set<Location> empty = world.getEmptySurroundingTiles(myLoc);
        if (empty.isEmpty()) return;

        Location best = null;
        int bestDist = -1;

        for (Location cand : empty) {
            int d = distance(cand, predatorLoc);
            if (d > bestDist) {
                bestDist = d;
                best = cand;
            }
        }

        if (best != null) {
            world.move(this, best);
            energy -= 5;
        }
    }

    /**
     * Alert nearby deer to flee as well.
     *
     * @param world the world in which the deer exists
     * @param myLoc the current location of this deer
     * @param predatorLoc the location of the predator
     * @param radius the radius to alert other deer
     */
    private void alertNearbyDeer(World world, Location myLoc, Location predatorLoc, int radius) {
        for (Location loc : world.getSurroundingTiles(myLoc, radius)) {
            Object o = world.getTile(loc);
            if (o instanceof Deer other && other != this) {
                other.isFleeing = true;

                Location otherLoc;
                try {
                    otherLoc = world.getLocation(other);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                if (otherLoc != null) {
                    other.flee(world, otherLoc, predatorLoc);
                }
            }
        }
    }

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    /**
     * Get the meat value provided by the deer when it dies.
     *
     * @return the meat value
     */
    @Override
    protected int getMeatValue() {
        return 70;
    }

    /**
     * Get display information for the deer based on its age, sleeping, and fleeing states.
     *
     * @return display information including color and image
     */
    @Override
    public DisplayInformation getInformation() {
        if (isChild()) {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "deer-small-sleeping");
            } else if (isFleeing) {
                return new DisplayInformation(Color.LIGHT_GRAY, "deer-small-afraid");
            } else {
                return new DisplayInformation(Color.GRAY, "deer-small");
            }
        } else {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "deer-sleeping");
            } else if (isFleeing) {
                return new DisplayInformation(Color.LIGHT_GRAY, "deer-afraid");
            } else {
                return new DisplayInformation(Color.DARK_GRAY, "deer");
            }
        }
    }
}
