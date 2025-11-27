import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;

public abstract class Animal implements Actor {
    private int age;
    protected int energy;
    protected boolean isAlive;
    protected boolean isSleeping;
    protected int amountOfKids;

    public Animal() {
        this.age = 0;
        this.energy = 100;
        this.isAlive = true;
        this.isSleeping = false;
        this.amountOfKids = 0;
    }

    public void act(World world) {
        age++;
        energy--;
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

    public int getAge() { return age; }
    public int getEnergy() { return energy; }
    public int getAmountOfKids() { return amountOfKids; }
}
