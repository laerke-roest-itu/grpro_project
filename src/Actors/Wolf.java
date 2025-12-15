package Actors;

import Inanimate.*;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Wolf extends Predator {
    private Pack pack;       // reference til ulvens flok
    private Den den;        // ulvens hule

    public Wolf(Pack pack) {
        super(240);
        this.random = new Random();
        this.shelter = null;
        this.pack = pack;
        if (pack != null) {
            pack.addMember(this);
        }
    }

    // ----------- ACT -----------

    @Override
    public void act(World world) {


        super.act(world);

        if (!isAlive || isSleeping) {    // 2) stop subclass-logik hvis dyret ikke skal gøre noget
            return;
        }

        if (getAge() >= getMaxAge() || getEnergy() <= 0) {
            die(world);
            return;
        }

        if (isAlive) {
            if (world.getCurrentTime() >= World.getDayDuration() - 3) {
                seekShelter(world);

            } else if (world.isDay()) {
                seekPack(world);

                if (isHungry()) {
                    hunt(world);
                } else {
                    moveRandomly(world);
                }

                if (den == null && pack != null && pack.getLeader() == this) {
                    buildDen(world);
                }


            } else if (world.isNight()) {
                if (den != null) {
                    List<Wolf> loveWolves = den.getWolves();
                    if ((loveWolves.size() >= 2) && pack != null && pack.getLeader() == this) {
                        sleep(world);
                        reproduce(world);
                    } else {
                        sleep(world);
                    }
                } else {
                    energy -= 5; // mister energi hvis ingen hule
                }
            }
        }
    }

    // ----------- LIFE -----------

    @Override
    protected void handleSleepLocation(World world) {
        if (den != null) {
            world.remove(this); // ulven sover i sin hule
        }
    }

    @Override
    public boolean isChild() {
        return getAge() < 40;
    }

    // ----------- EATING -----------

    @Override
    public void eat(World world, Location targetLoc) {
        Object o = world.getTile(targetLoc);

        if (o instanceof Carcass carcass) {
            int amount = Math.min(20, carcass.getMeatLeft());
            carcass.eaten(amount);
            energy += amount;
        }
        // hvis det ikke er Carcass → gør ingenting
    }

    @Override
    public boolean canEat(Object object) {
        return object instanceof Carcass;
    }

    @Override
    protected int getFoodEnergy(Object object) {
        if (object instanceof Rabbit) return 40;
        return 0;
    }

    @Override
    protected int getMeatValue() {
        return 50;
    }

    // ----------- REPRODUCTION -----------

    @Override
    protected Animal createChild(World world, Location childLoc) {
        return new Wolf(pack);
    }

    @Override
    protected Location getReproductionLocation(World world) {
        if (den != null) {
            Location denLocation = world.getLocation(den);
            Set<Location> emptyAround = world.getEmptySurroundingTiles(denLocation);

            if (world.isTileEmpty(denLocation)) {
                return denLocation;
            } else if (!emptyAround.isEmpty()) {
                return emptyAround.iterator().next();
            }
        }
        return null;
    }


    // ----------- PACK/DEN -----------

    private void seekPack(World world) {
        if (pack == null) return;

        Wolf leader = pack.getLeader();
        if (leader == null || leader == this) return;

        Location leaderLoc;
        try {
            leaderLoc = world.getLocation(leader);
        } catch (IllegalArgumentException e) {
            return; // lederen findes ikke i verden lige nu
        }

        if (leaderLoc == null) return;

        moveOneStepTowards(world, leaderLoc); // vælg energipris
    }

    public void setMember(Pack pack) {
        this.pack = pack;
    }

    public Pack getPack() {
        return pack;
    }

    public void buildDen(World world) {
        Location wolfLoc;

        // BESKYTTELSE: prøv at hente lokation, men giv op hvis ulven ikke er i verden
        try {
            wolfLoc = world.getLocation(this);
        } catch (IllegalArgumentException e) {
            return; // ulven findes ikke i verden → gør ingenting
        }

        if (wolfLoc == null) return;

        // Kun byg en hule hvis ulven ikke allerede har en
        // og feltet er tomt (ingen blocking object)
        if (den == null && world.isTileEmpty(wolfLoc)) {
            den = new Den();
            world.setTile(wolfLoc, den);
            den.addWolf(this);

            if (pack != null && pack.getLeader() == this) {
                pack.claimDen(den);
            }
        }
    }

    public void setDen(Den den) {
        this.den = den;
    }

    @Override
    protected Set<Location> getHuntingArea(World world) {
        Location wolfLoc = world.getLocation(this);
        // fx radius 2 som før
        return world.getSurroundingTiles(wolfLoc, 2);
    }

    @Override
    protected int getHuntMoveCost() {
        return 8;   // samme som før
    }

    @Override
    public boolean isEnemyPredator(Animal other) {
        if (!(other instanceof Predator)) return false;

        // Hvis det er en ulv
        if (other instanceof Wolf otherWolf) {
            // samme pack → ikke fjende
            if (this.pack != null && this.pack == otherWolf.getPack()) {
                return false;
            }
            // ellers → fjende
            return true;

        } else if (other instanceof Bear) {
            return true;
        }

        return false;
    }

    @Override
    public int getAttackDamage() {
        return 10;
    }

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    @Override
    public DisplayInformation getInformation() {
        if (isChild()) {
            if (isSleeping) {
                return new DisplayInformation(Color.GRAY, "wolf-small-sleeping"); // billede af unge ulv
            } else {
                return new DisplayInformation(Color.GRAY, "wolf-small"); // billede af unge ulv
            }
        } else {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "wolf-sleeping");
            } else {
                return new DisplayInformation(Color.DARK_GRAY, "wolf"); // billede af voksen ulv
            }
        }
    }
}
