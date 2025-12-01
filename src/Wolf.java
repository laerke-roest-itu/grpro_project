import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
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

        if (getAge() >= 240 || getEnergy() <= 0) {
            die(world);
            return;
        }

        if (world.isDay()) {
            seekPack(world);
            checkForEnemyWolves(world);
            if (getEnergy() < 50) {
                hunt(world);
            }
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
    @Override
    public void eat(World world, Location targetLoc) {
        Object o = world.getTile(targetLoc);
        if (o != null && canEat(o)) {
            world.delete(o);
            energy += getFoodEnergy(o);
        }
    }

    private void hunt(World world) {
        Location wolfLoc = world.getLocation(this);

        // Find alle felter inden for radius 2
        Set<Location> radiusTiles = world.getSurroundingTiles(wolfLoc, 2);

        Location targetPreyLoc = null;
        int bestDistance = Integer.MAX_VALUE;

        // 1. Find det nærmeste bytte
        for (Location loc : radiusTiles) {
            Object o = world.getTile(loc);
            if (o != null && canEat(o)) {
                int dist = distance(wolfLoc, loc);
                if (dist < bestDistance) {
                    bestDistance = dist;
                    targetPreyLoc = loc;
                }
            }
        }

        // 2. Hvis der ikke er bytte, gør ingenting
        if (targetPreyLoc == null) return;

        // 3. Hvis ulven allerede står ved siden af byttet → spis
        Set<Location> neighbors = world.getSurroundingTiles(wolfLoc);
        if (neighbors.contains(targetPreyLoc)) {
            eat(world, targetPreyLoc);
            return;
        }

        // 4. Ellers: bevæg dig ét skridt tættere på det nærmeste bytte
        moveOneStepTowards(world, targetPreyLoc, 8); // energikost fx 8
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
            energy -= 5;
        }
    }

    private void checkForEnemyWolves(World world) {
        Location wolfLoc = world.getLocation(this);
        Set<Location> neighbors = world.getSurroundingTiles(wolfLoc);

        for (Location loc : neighbors) {
            Object o = world.getTile(loc);
            if (o instanceof Wolf otherWolf) {
                // spring over hvis det er os selv
                if (otherWolf == this) continue;

                // samme pack → ingen kamp
                if (this.pack != null && this.pack == otherWolf.getPack()) continue;

                // ellers: kamp!
                fight(otherWolf, world);
                return; // kun én kamp pr. tur
            }
        }
    }

    private void fight(Wolf opponent, World world) {
        if (this.getEnergy() > opponent.getEnergy()) {
            // denne ulv vinder
            opponent.die(world);
            this.energy -= 10; // kamp koster energi
        } else if (this.getEnergy() < opponent.getEnergy()) {
            // modstanderen vinder
            this.die(world);
            opponent.energy -= 10;
        } else {
            // lige energi → den ældste ulv vinder
            if (this.getAge() > opponent.getAge()) {
                opponent.die(world);
                this.energy -= 10;
            } else if (this.getAge() < opponent.getAge()) {
                this.die(world);
                opponent.energy -= 10;
            } else {
                // hvis både energi og alder er ens → tilfældig vinder
                if (random.nextBoolean()) {
                    opponent.die(world);
                    this.energy -= 10;
                } else {
                    this.die(world);
                    opponent.energy -= 10;
                }
            }
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

    @Override
    public DisplayInformation getInformation() {
        if (getAge() < 40) {
            return new DisplayInformation(Color.GRAY, "wolf-small"); // billede af unge ulv
        } else {
            return new DisplayInformation(Color.DARK_GRAY, "wolf"); // billede af voksen ulv
        }
    }
}
