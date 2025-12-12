package Actors;

import Inanimate.Burrow;
import Inanimate.Grass;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Rabbit extends Animal {
    private Burrow burrow;
    private final Random random;

    public Rabbit() {
        super(); // kalder Actors.Animal's constructor
        this.random = new Random();
        this.shelter = burrow;
    }

    public Rabbit(Random random) {
        super();
        this.random = random;
        this.shelter = burrow;
    }

    // ----------- ACT -----------

    @Override
    public void act(World world) {
        super.act(world);

        /*if (!isAlive || isSleeping) {    // 2) stop subclass-logik hvis dyret ikke skal gøre noget
            return;
        }*/

        if (getAge() >= 180 || getEnergy() <= 0) {
            die(world);
            return;
        }

        if (isAlive) {
            //hvis det er skumring, så gør kaninen det her:
            if (world.getCurrentTime() >= World.getDayDuration() - 3) {
                seekShelter(world);

            } else if (world.isDay()) { //hvis det er dag, så gør kaninen det her:

                if (world.getCurrentTime() == 0 && burrow != null) {
                    wakeUp(world);
                }

                // brug fælles moveRandomly, men få moveTo tilbage
                Location moveTo = moveRandomly(world);

                if (moveTo != null && getEnergy() < 50) {
                    eat(world, moveTo);
                }

                //hvis kaninen ikke har et hul, vil den i løbet af dagen måske grave et, måske claim et
                //højere chance for at claim, da kaninen skal stå på et burrow for at claim det
                if (burrow == null) {
                    if (random.nextDouble() < 0.25) {
                        digBurrow(world);
                    } else if (random.nextDouble() < 0.5) {
                        claimBurrow(world);
                    }
                }

            }  else if (world.isNight()) { //hvis det er nat, så gør kaninen det her:
                if (burrow != null) {
                    // hvis der er mindst 2 kaniner i samme hul:
                    List<Rabbit> loveRabbits = burrow.getRabbits();
                    if (loveRabbits.size() >= 2 && isLeaderInBurrow()) {
                        reproduce(world);
                        sleep(world);
                    } else {
                        sleep(world);
                    }
                } else {
                    // den er ude i det fri og mister bare energi i stedet for at sove
                    energy -= 5;
                    //sleep(world);
                }
            }
        }
    }

    @Override
    public void seekShelter(World world) {
        if (burrow == null) return;
        Location burrowLoc = world.getLocation(burrow);
        // 10 fordi det i din logik er dyrere at søge mod hul
        moveOneStepTowards(world, burrowLoc, 10);
    }

    // ----------- LIFE -----------

    @Override
    public void wakeUp(World world) {
        if (burrow != null) {
            Location burrowLoc = world.getLocation(burrow);
            world.setTile(burrowLoc, this);
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
        if (burrow != null) {
            Location burrowLocation = world.getLocation(burrow);
            Set<Location> emptyTilesAroundBurrow = world.getEmptySurroundingTiles(burrowLocation);

            if (world.isTileEmpty(burrowLocation)) {
                return burrowLocation;
            } else if (!emptyTilesAroundBurrow.isEmpty()) {
                return emptyTilesAroundBurrow.iterator().next();
            }
        }
        return null;
    }

    // ----------- BURROW -----------

    public void digBurrow(World world) {
        Location rabbitLocation = world.getLocation(this);

        Object obj = world.getNonBlocking(rabbitLocation);
        if (!(obj instanceof Burrow)) {
            if (obj instanceof Grass) {
                world.delete(obj);
            }
            Burrow newBurrow = new Burrow();
            world.setTile(rabbitLocation, newBurrow);
            burrow = newBurrow;
            burrow.addRabbit(this);
        }
    }

    public void claimBurrow(World world) {
        //kaninens lokation:
        Location rabbitLocation = world.getLocation(this);

        //tjek om der står et burrow på feltet
        Object obj = world.getNonBlocking(rabbitLocation);
        if (obj instanceof Burrow) { //altså hvis den står på et burrow
            burrow = (Burrow) obj;
            burrow.addRabbit(this);
        }
    }

    public Burrow getBurrow() {return burrow;}

    public void setBurrow(Burrow burrow) {
        this.burrow = burrow;
        if (burrow != null) {
            burrow.addRabbit(this); // så hullet også "kender" kaninen
        }
    }

    private boolean isLeaderInBurrow() {
        return burrow != null && !burrow.getRabbits().isEmpty()
                && burrow.getRabbits().getFirst() == this;
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
