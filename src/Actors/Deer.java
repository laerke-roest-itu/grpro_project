package Actors;

import Inanimate.DeerPack;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.Set;
public class Deer extends Herbivore {
    private DeerPack pack;
    private boolean isFleeing;

    public Deer(DeerPack pack) {
        super(300);
        this.pack = pack;
        this.isFleeing = false;

        if (pack != null) {
            pack.addMember(this);
        }
    }

    // shelter = pack home (Location), så vi override'r
    @Override
    public void seekShelter(World world) {
        if (pack == null) return;
        Location home = pack.getHome();
        if (home == null) return;
        moveOneStepTowards(world, home);
    }

    // ----------- ACT -----------

    @Override
    public void act(World world) {
        // vigtig sikkerhed (undgår "Object does not exist in the world")
        Location myLoc;
        try {
            myLoc = world.getLocation(this);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (myLoc == null) return;

        // hvis jeg er leader og pack.home ikke er sat endnu => sæt den én gang
        if (pack != null && pack.getHome() == null && pack.getLeader() == this) {
            pack.setHome(myLoc); // leaderens startposition
        }

        // rovdyr i nærheden?
        Location predatorLoc = findNearbyPredator(world, myLoc, 2);
        if (predatorLoc != null) {
            isFleeing = true;
            flee(world, myLoc, predatorLoc);
            alertNearbyDeer(world, myLoc, predatorLoc, 2);
            return; // flygt bruger hele turen
        }

        isFleeing = false;
        // normal herbivore adfærd (move + eat + shelter logic)
        super.act(world);
    }

    /*private void seekPack(World world) {
        if (pack == null) return;

        Deer leader = pack.getLeader();
        if (leader == null || leader == this) return;

        Location leaderLoc;
        try {
            leaderLoc = world.getLocation(leader);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (leaderLoc == null) return;

        moveOneStepTowards(world, leaderLoc);
    }*/

    /** Deer dag: hold sammen med flokken (ellers random + spis) */
    @Override
    protected void dayBehaviour(World world) {
        if (pack != null) {
            Deer leader = pack.getLeader();

            if (leader != null && leader != this) {
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

                    // Hvis for langt væk → saml flokken
                    if (dist > 3) {
                        moveOneStepTowards(world, leaderLoc);
                        return; // brug hele turen på at følge lederen
                    }
                }
            }
        }

        // Ellers: normal planteæder-adfærd
        Location moveTo = moveRandomly(world);
        if (moveTo != null && isHungry()) {
            eat(world, moveTo);
        }
    }



    /** Deer nat: gå mod home og sov når man er fremme */
    @Override
    protected void nightBehaviour(World world) {
        // hvis vi har home -> søg shelter (home) i natten også (valgfrit)
        // eller bare sov hvis du vil
        seekShelter(world);
        // hvis du vil “kun sove når fremme”, kan du tjekke distance:
        Location myLoc;
        try { myLoc = world.getLocation(this); }
        catch (IllegalArgumentException e) { return; }

        Location home = (pack == null) ? null : pack.getHome();
        if (myLoc != null && home != null && distance(myLoc, home) == 0) {
            sleep(world);
        }
    }

    // ----------- FLEEING -----------

    private Location findNearbyPredator(World world, Location from, int radius) {
        for (Location loc : world.getSurroundingTiles(from, radius)) {
            Object o = world.getTile(loc);
            if (o instanceof Predator) {
                return loc;
            }
        }
        return null;
    }

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

    public void setPack(DeerPack pack) {
        this.pack = pack;
    }

    public DeerPack getPack() {
        return pack;
    }

    @Override
    protected int getMeatValue() {
        return 70; // eksempel
    }

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
