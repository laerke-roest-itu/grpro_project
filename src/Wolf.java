import itumulator.world.Location;
import itumulator.world.World;
import java.util.*;

public class Wolf extends Animal {
    private Pack pack;       // reference til ulvens flok
    private Den den;         // ulvens hule

    public Wolf(Pack pack) {
        super();
        this.random = new Random();
        this.den = null;
        this.pack = pack;
        if (pack != null) {
            pack.addWolf(this);
        }
    }

    @Override
    public void act(World world) {
        super.act(world); // fælles aging + energitab

        if (getAge() >= 300 || getEnergy() <= 0) {
            die(world);
            return;
        }

        // Ulve jager om dagen
        if (world.isDay()) {
            hunt(world);
            seekPack(world);
        }

        // Om natten kan de hvile i hulen og reproducere
        else if (world.isNight()) {
            if (den != null) {
                sleep(world);
                reproduce(world);
            } else {
                energy -= 5; // mister energi hvis ingen hule
            }
        }
    }

    // ---------- Jagt ----------

    private void hunt(World world) {
        Location wolfLoc = world.getLocation(this);
        Set<Location> neighbors = world.getSurroundingTiles(wolfLoc);

        for (Location loc : neighbors) {
            if (world.containsNonBlocking(loc)) {
                Object prey = world.getNonBlocking(loc);

                // Ulve spiser kaniner (og evt. andre dyr)
                if (prey instanceof Rabbit) {
                    world.delete(prey);
                    energy += 30; // kød giver meget energi
                    break;        // én jagt pr. tur
                }
            }
        }
    }

    // ---------- Flokdyr ----------

    private void seekPack(World world) {
        if (pack == null) return;

        Location wolfLoc = world.getLocation(this);
        // simpelt eksempel: bevæg dig mod første ulv i flokken
        Wolf leader = pack.getLeader();
        if (leader != null && leader != this) {
            Location leaderLoc = world.getLocation(leader);
            moveTowards(world, wolfLoc, leaderLoc);
        }
    }

    private void moveTowards(World world, Location from, Location to) {
        Set<Location> neighbors = world.getEmptySurroundingTiles(from);
        Location bestMove = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Location loc : neighbors) {
            int dist = Math.abs(loc.getX() - to.getX()) + Math.abs(loc.getY() - to.getY());
            if (dist < bestDistance) {
                bestDistance = dist;
                bestMove = loc;
            }
        }

        if (bestMove != null) {
            world.move(this, bestMove);
            energy -= 10;
        }
    }

    // ---------- Metoder fra Animal ----------

    @Override
    protected boolean canEat(Object object) {
        return (object instanceof Rabbit); // ulve spiser kaniner
    }

    @Override
    protected int getFoodEnergy(Object object) {
        return 30; // kød giver mere energi end græs
    }

    @Override
    protected void handleSleepLocation(World world) {
        if (den != null) {
            world.remove(this); // ulven sover i sin hule
        }
    }

    @Override
    protected Animal createChild() {
        return new Wolf(pack);
    }

    @Override
    protected Location getReproductionLocation(World world) {
        if (den != null) {
            return world.getLocation(den);
        }
        return null;
    }

    // ---------- Hjælpefunktioner ----------

    public void setPack(Pack pack) {
        this.pack = pack;
        pack.addWolf(this);
    }

    public Pack getPack() {
        return pack;
    }

    public void buildDen(World world) {
        Location wolfLoc = world.getLocation(this);
        if (den == null) {
            den = new Den();
            world.setTile(wolfLoc, den);
            den.addWolf(this);
        }
    }
}
