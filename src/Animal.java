import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class Animal implements Actor {
    protected int age;
    protected int energy;
    protected boolean isAlive;
    protected boolean isSleeping;
    protected int amountOfKids;
    protected Random random;

    public Animal() {
        this.age = 0;
        this.energy = 100;
        this.isAlive = true;
        this.isSleeping = false;
        this.amountOfKids = 0;
        this.random = new Random();
    }

    // Fælles "tick" for alle dyr: alder og basis-energitab
    protected void tickCommon(World world) {
        age++;
        energy--;
    }

    // Fælles random-bevægelse, hvis et dyr *vælger* at bruge den
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

    // Default-implementation, som simple dyr kan bruge
    @Override
    public void act(World world) {
        tickCommon(world);
        moveRandomly(world);
    }

    public void eat(World world, Location targetLoc) {
        if (world.containsNonBlocking(targetLoc)) {
            Object object = world.getNonBlocking(targetLoc);
            if (canEat(object)) {
                world.delete(object);
                energy += getFoodEnergy(object);
            }
        }
    }

    protected abstract boolean canEat(Object object);
    protected abstract int getFoodEnergy(Object object);

    // I Animal:
    protected Location moveOneStepTowards(World world, Location target, int energyCost) {
        // hvor står dyret nu?
        Location currentLoc = world.getLocation(this);
        if (currentLoc == null || target == null) return null;

        // find tomme naboer
        Set<Location> emptyNeighbors = world.getEmptySurroundingTiles(currentLoc);
        if (emptyNeighbors.isEmpty()) return null;

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

        return bestMove;
    }


    public void sleep(World world) {
        isSleeping = true;
        handleSleepLocation(world);
        energy += getSleepEnergy();
    }

    protected int getSleepEnergy() {
        return 50; // standardværdi, kan overskrives
    }

    protected abstract void handleSleepLocation(World world);

    public void wakeUp(World world) {
        isSleeping = false;
        Location wakeUpLoc = world.getLocation(this);
        world.setTile(wakeUpLoc, this);
    }

    public void reproduce(World world) {
        Animal child = createChild();
        Location loc = getReproductionLocation(world);
        if (loc != null) {
            world.setTile(loc, child);
            amountOfKids++;
            energy -= 15;
        }
    }

    protected abstract Animal createChild();
    protected abstract Location getReproductionLocation(World world);

    public void die(World world) {
        isAlive = false;
        world.delete(this);
    }

    public int distance(Location a, Location b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    public int getAge() { return age; }
    public int getEnergy() { return energy; }
    public int getAmountOfKids() { return amountOfKids; }
}
