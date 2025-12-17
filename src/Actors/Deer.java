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
    private Location territoryCenter;
    private Herd herd;
    private boolean isFleeing;

    /** Constructor for Deer for test.
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

    /** Constructor for Deer with specified territory center.
     *
     * @param herd the herd to which the deer belongs
     * @param territoryCenter the center location of the deer's territory
     */
    public Deer(Herd herd, Location territoryCenter) {
        super(300);
        this.herd = herd;
        this.isFleeing = false;
        this.territoryCenter = territoryCenter;
        this.shelter = territoryCenter;

        if (herd != null) {
            herd.addMember(this);
        }
    }

    /** Seek shelter at the herd's home location.
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

    /** Deer act: flee from predators, else normal herbivore behavior
     *
     * @param world the world in which the deer acts
     */
    @Override
    public void act(World world) {
        Location myLoc;
        try {
            myLoc = world.getLocation(this);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (myLoc == null) return;

        if (!isAlive) return;

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
        super.act(world);
    }

    /** Deer dag: hold sammen med flokken (ellers random + spis) */
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
                        return;
                    }

                    if (myLoc != null && leaderLoc != null) {
                        int dist = distance(myLoc, leaderLoc);


                        if (dist > 2) {
                            moveOneStepTowards(world, leaderLoc);
                            return;
                        }
                    }
                }
            }
        }
    }

    /** Deer night: go towards home and sleep when reached location*/
    @Override
    public void nightBehaviour(World world) {
        super.nightBehaviour(world);
        Location myLoc;
        try { myLoc = world.getLocation(this); }
        catch (IllegalArgumentException e) { return; }

        /*
        Location home = (herd == null) ? null : herd.getHome();
        if (myLoc != null && home != null && distance(myLoc, home) == 0) {
            sleep(world);
        } */

    }

    @Override
    protected void handleSleepLocation(World world) {
        Location deerLoc = world.getLocation(this);

        if (deerLoc.equals(territoryCenter) || distance(deerLoc, territoryCenter) <= 1) {
            isSleeping = true;
            energy += 50;
        } else {
            if (world.isTileEmpty(territoryCenter)) {
                moveOneStepTowards(world, territoryCenter);
            } else {
                Set<Location> neighbors = world.getEmptySurroundingTiles(territoryCenter);
                if (!neighbors.isEmpty()) {
                    Location spot = neighbors.iterator().next();
                    moveOneStepTowards(world, spot);
                }
            }
        }
    }

    // ----------- FLEEING -----------

    /** Find nearby predator within a given radius.
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

    /** Flee from a predator by moving to the farthest empty tile.
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

    /** Alert nearby deer to flee as well.
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

    /** Get the meat value provided by the deer when it dies.
     *
     * @return the meat value
     */
    @Override
    protected int getMeatValue() {
        return 70;
    }

    /** Get display information for the deer based on its state.
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
                return new DisplayInformation(Color.GRAY, "deer-small"); // billede af hjorteunge
            }
        } else {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "deer-sleeping");
            } else if (isFleeing) {
                return new DisplayInformation(Color.LIGHT_GRAY, "deer-afraid");
            } else {
                return new DisplayInformation(Color.DARK_GRAY, "deer"); // billede af voksen hjort
            }
        }
    }
}
