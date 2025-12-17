package Actors;

import Inanimate.Burrow;
import Inanimate.Fungi;
import Inanimate.Grass;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Rabbit extends Herbivore {
    private Burrow burrow;
    private final Random random;

    public Rabbit() {
        super(180); // kalder Actors.Animal's constructor
        this.random = new Random();
    }

    public Rabbit(Random random) {
        super(180);
        this.random = random;
    }

    // ----------- ACT -----------

    /*@Override
    public void act(World world) {
        super.act(world); // kører Herbivore-logikken (tick+dag+skumring)

        // hvis den døde eller sover efter super: stop
        if (!isAlive || isSleeping) return;

        // kun burrow-ting i dag (ikke i skumring/nat)
        if (world.isDay() && burrow == null) {
            double r = random.nextDouble();
            if (r < 0.25) digBurrow(world);
            else if (r < 0.50) claimBurrow(world);
        }

        if (world.isNight()) {
            if (burrow != null) {
                List<Rabbit> loveRabbits = burrow.getRabbits();
                if (loveRabbits.size() >= 2 && isLeaderInBurrow()) {
                    reproduce(world);
                }
                sleep(world);
            } else {
                energy -= 5;
            }
        }
    }*/

    // --- DAG ---
    @Override
    protected void dayBehaviour(World world) {
        super.dayBehaviour(world); // move + eat hvis sulten

        // Burrow-ting kun i dag, og kun hvis vi ikke har et
        if (burrow == null) {
            double r = random.nextDouble();
            if (r < 0.25) digBurrow(world);
            else if (r < 0.50) claimBurrow(world);
        }
    }

    // --- NAT ---
    @Override
    protected void nightBehaviour(World world) {
        if (burrow != null) {
            // reproduktion hvis mindst 2 og jeg er "leder" i hulen
            List<Rabbit> loveRabbits = burrow.getRabbits();
            if (loveRabbits.size() >= 2 && isLeaderInBurrow()) {
                reproduce(world);
            }
            sleep(world);
        } else {
            energy -= 5; // ingen shelter
        }
    }

    @Override
    public void seekShelter(World world) {
        if (burrow == null) return;

        Location burrowLoc;
        try {
            burrowLoc = world.getLocation(burrow);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (burrowLoc == null) return;

        moveOneStepTowards(world, burrowLoc);
    }


    // ----------- LIFE -----------

    @Override
    public void wakeUp(World world) {
        if (burrow == null) return;

        Location burrowLoc;
        try {
            burrowLoc = world.getLocation(burrow);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (burrowLoc == null) return;


        // 1) prøv at stå på burrow-tile hvis den er fri
        if (world.isTileEmpty(burrowLoc)) {
            world.setTile(burrowLoc, this);
            isSleeping = false;
            return;
        }

        // 2) ellers: find et tomt nabofelt
        Set<Location> empty = world.getEmptySurroundingTiles(burrowLoc);
        if (!empty.isEmpty()) {
            world.setTile(empty.iterator().next(), this);
            isSleeping = false;
        }
    }


    @Override
    protected void handleSleepLocation(World world) {
        if (burrow != null) {
            world.remove(this); // Actors.Rabbit sover I burrow
        }
    }

    @Override
    protected int getSleepEnergy() { return 25; }

    // ----------- EATING -----------

    @Override
    public void eat(World world, Location targetLoc) {
        Object o = world.getNonBlocking(targetLoc);
        if (canEat(o)) {
            energy += getFoodEnergy(o);
            world.delete(o); // græs forsvinder
        }
    }

    @Override
    protected boolean canEat(Object object) {
        return object instanceof Grass;
    }

    @Override
    protected int getFoodEnergy(Object object) {
        return 20;
    }

    protected int getMeatValue() {
        return 20;
    }

    // ----------- REPRODUCTION -----------

    @Override
    protected Animal createChild(World world, Location childLoc) {
        Rabbit child = new Rabbit();
        child.setBurrow(this.burrow);
        return child;
    }

    @Override
    protected Location getReproductionLocation(World world) {
        if (burrow == null) return null;

        Location burrowLocation;
        try {
            burrowLocation = world.getLocation(burrow);
        } catch (IllegalArgumentException e) {
            return null; // burrow findes ikke i verden længere
        }

        if (burrowLocation == null) return null;

        // først: prøv selve burrow-feltet
        if (world.isTileEmpty(burrowLocation)) {
            return burrowLocation;
        }

        // ellers: find et tomt nabofelt
        Set<Location> emptyTilesAroundBurrow = world.getEmptySurroundingTiles(burrowLocation);
        if (!emptyTilesAroundBurrow.isEmpty()) {
            return emptyTilesAroundBurrow.iterator().next();
        }

        return null;
    }


    // ----------- BURROW -----------

    public void digBurrow(World world) {
        Location rabbitLocation = world.getLocation(this);
        Object obj = world.getNonBlocking(rabbitLocation);

        if (obj instanceof Burrow b) {
            setBurrow(b);   // valgfrit: claim eksisterende
            return;
        }

        if (obj != null && !(obj instanceof Grass || obj instanceof Fungi)) {
            return;         // noget andet non-blocking → grav ikke
        }

        if (obj instanceof Grass || obj instanceof Fungi) {
            world.delete(obj);
        }

        Burrow newBurrow = new Burrow();
        world.setTile(rabbitLocation, newBurrow);
        setBurrow(newBurrow);
    }


    public void claimBurrow(World world) {
        Location rabbitLocation = world.getLocation(this);
        Object obj = world.getNonBlocking(rabbitLocation);

        if (obj instanceof Burrow b) {
            setBurrow(b);               // <-- i stedet for burrow = (Burrow) obj; ...
        }
    }


    public Burrow getBurrow() {return burrow;}

    public void setBurrow(Burrow burrow) {
        this.burrow = burrow;
        this.shelter = burrow;          // <-- vigtig
        if (burrow != null) {
            burrow.addRabbit(this);
        }
    }


    private boolean isLeaderInBurrow() {
        return burrow != null && !burrow.getRabbits().isEmpty()
                && burrow.getRabbits().get(0) == this;
    }

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    @Override
    public boolean isChild() {
        return getAge() < 10;
    }

    @Override
    public DisplayInformation getInformation() {
        if (isChild()) { //
            if (isSleeping) {
                return new DisplayInformation(Color.GRAY, "rabbit-small-sleeping");
            } else {
                return new DisplayInformation(Color.GRAY, "rabbit-small"); // billede af kaninunge
            }
        } else {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "rabbit-sleeping");
            } else {
                return new DisplayInformation(Color.DARK_GRAY, "rabbit-large"); // billede af voksen kanin
            }
        }
    }
}
