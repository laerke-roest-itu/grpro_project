package Actors;

import Inanimate.Carcass;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class Animal implements Actor, DynamicDisplayInformationProvider {
    protected int age;
    protected int energy;
    protected boolean isAlive;
    protected boolean isSleeping;
    protected int amountOfKids;
    protected Random random;
    protected Object shelter;

    public Animal() {
        this.age = 0;
        this.energy = 100;
        this.isAlive = true;
        this.isSleeping = false;
        this.amountOfKids = 0;
        this.random = new Random();
    }

    // ----------- ACT -----------

    @Override
    public void act(World world) {
        if (!isAlive || isSleeping) return;
        tickCommon();
    }

    protected void tickCommon() {
        age++;
        energy--;
    }

    protected Location moveRandomly(World world) {
        Location animalLocation = world.getLocation(this);
        Set<Location> emptyTilesNearAnimal = world.getEmptySurroundingTiles(animalLocation);

        if (emptyTilesNearAnimal.isEmpty()) return null;
        List<Location> listOfPlacesToMove = new ArrayList<>(emptyTilesNearAnimal);
        int j = random.nextInt(emptyTilesNearAnimal.size());
        Location moveTo = listOfPlacesToMove.get(j);
        world.move(this, moveTo);
        energy -= 5;

        return moveTo;
    }

    protected void moveOneStepTowards(World world, Location target, int energyCost) {
        // hvor står dyret nu?
        Location currentLoc = world.getLocation(this);
        if (currentLoc == null || target == null) return;

        // find tomme naboer
        Set<Location> emptyNeighbors = world.getEmptySurroundingTiles(currentLoc);
        if (emptyNeighbors.isEmpty()) return;

        Location bestMove = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Location loc : emptyNeighbors) {
            int d = distance(loc, target);
            if (d < bestDistance) {
                bestDistance = d;
                bestMove = loc;
            }
        }

        if (bestMove != null) {
            world.move(this, bestMove);
            energy -= energyCost;
        }
    }

    public void seekShelter(World world) {
        if (shelter == null) return;

        Location myLoc = world.getLocation(this);
        Location shelterLoc = world.getLocation(shelter);

        // Hvis vi allerede står på shelter → gå i seng
        if (myLoc.equals(shelterLoc)) {
            handleSleepLocation(world); // kobling til eksisterende sleep-logik
            return;
        }
        // Ellers bevæg os ét skridt mod shelter
        moveOneStepTowards(world, shelterLoc, 10); // energikost fx 10
    }

    // ----------- LIFE -----------

    public void die(World world) {
        isAlive = false;

        Location loc = world.getLocation(this);
        if (loc != null) {
            int meat = getMeatValue(); // afhænger af art
            int rot = 25;              // fx antal ticks før ådslet rådner væk
            Carcass carcass = new Carcass(meat, rot);

            world.delete(this);        // fjern det levende dyr
            world.setTile(loc, carcass); // placer ådslet
        }
    }

    public void sleep(World world) {
        isSleeping = true;
        handleSleepLocation(world);
        energy += getSleepEnergy();
    }

    protected abstract void handleSleepLocation(World world);

    protected int getSleepEnergy() {
        return 50; // standardværdi, kan overskrives
    }

    public void wakeUp(World world) {
        isSleeping = false;
    }

    // ----------- EATING -----------

    public void eat(World world, Location targetLoc) {}

    protected abstract boolean canEat(Object object);

    protected abstract int getFoodEnergy(Object object);

    protected abstract int getMeatValue();

    // ----------- REPRODUCTION -----------

    public void reproduce(World world) {
        if (energy < 15 || (isChild())) return;
        Location loc = getReproductionLocation(world);
        Animal child = createChild(world, loc);
        if (loc != null) {
            world.setTile(loc, child);
            amountOfKids++;
            energy -= 15;
        }
    }

    protected abstract Animal createChild(World world, Location childLoc);

    protected abstract Location getReproductionLocation(World world);

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    public boolean isChild() {
        return getAge() < 50;
    }

    public int distance(Location a, Location b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    public int getAge() { return age; }

    public int getEnergy() { return energy; }

    public void setEnergy(int i) {
        energy = i;
    }

    public int getAmountOfKids() { return amountOfKids; }

}
