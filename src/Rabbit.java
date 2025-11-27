import itumulator.world.Location;
import itumulator.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Rabbit extends Animal {
    private Burrow burrow;
    private final Random random;

    public Rabbit() {
        super(); // kalder Animal's constructor
        this.random = new Random();
        this.burrow = null;
    }

    public Rabbit(Random random) {
        super();
        this.random = random;
        this.burrow = null;
    }

    @Override
    public void act(World world) {
        super.act(world); // fælles aging + energitab fra Animal

        //til udregning af hvornår den skal søge mod hul:
        int currentTime = world.getCurrentTime();
        int dayDuration = World.getDayDuration();

        if (getAge() >= 180 || getEnergy() <= 0) {
            die(world);
            return;

        } else if (isAlive) {
            //hvis det er skumring, så gør kaninen det her:
            if (currentTime >= dayDuration - 3) {
                //hvis den har et hul at krybe i:
                if (burrow != null) {
                    seekBurrow(world);
                }

            } //hvis det er dag, så gør kaninen det her:
            else if (world.isDay()) {
                Location rabbitLocation = world.getLocation(this);
                Set<Location> emptyTilesNearRabbit = world.getEmptySurroundingTiles(rabbitLocation);
                List<Location> listOfPlacesToMove = new ArrayList<>(emptyTilesNearRabbit);

                if (currentTime == 0 && burrow != null) {
                    wakeUp(world);
                }

                if (!emptyTilesNearRabbit.isEmpty()) {
                    int j = random.nextInt(emptyTilesNearRabbit.size());
                    Location moveTo = listOfPlacesToMove.get(j);
                    world.move(this, moveTo);
                    energy -= 5;
                    if (getEnergy() < 50) {
                        eat(world, moveTo);
                    }
                }

                //hvis kaninen ikke har et hul, vil den i løbet af dagen måske grave et, måske claime et
                //højere chance for at claime, da kaninen skal stå på et burrow for at claime det
                if (burrow == null) {
                    if (random.nextDouble() < 0.25) {
                        digBurrow(world);
                    } else if (random.nextDouble() < 0.5) {
                        claimBurrow(world);
                    }
                }

                //hvis det er nat, så gør kaninen det her:
            }  else if (world.isNight()) {
                if (burrow != null) {
                    // hvis der er mindst 2 kaniner i samme hul:
                    List<Rabbit> loveRabbits = burrow.getRabbits();
                    if (loveRabbits.size() >= 2 && isLeaderInBurrow()) {
                        sleep(world);
                        reproduce(world);
                    } else {
                        sleep(world);
                    }
                } else {
                    // den er ude i det fri og mister bare energi i stedet for at sove
                    energy -= 5;
                }
            }

        }
    }

    @Override
    protected boolean canEat(Object object) {
        return (object instanceof Grass);
    }

    @Override
    protected int getFoodEnergy(Object object) {
        return 10;
    }

    @Override
    protected void handleSleepLocation(World world) {
        if (burrow != null) {
            world.remove(this); // Rabbit sover i burrow
        }
    }

    @Override
    protected int getSleepEnergy() { return 25; }


    @Override
    public void wakeUp(World world) {
        if (burrow != null) {
            Location burrowLoc = world.getLocation(burrow);
            world.setTile(burrowLoc, this);
        }
    }

    public void setEnergy(int i) { //til test
        energy = i;
    }

    @Override
    protected Animal createChild() {
        return new Rabbit();
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

    public void seekBurrow(World world) {
        Location rabbitLoc = world.getLocation(this);
        Location burrowLoc = world.getLocation(burrow);

        //find alle tomme nabofelter
        Set<Location> neighbors = world.getEmptySurroundingTiles(rabbitLoc);
        Location bestMove = null;
        int bestDistance = Integer.MAX_VALUE;

        //vælg det nabofelt der minimerer afstanden mest til burrow
        for (Location loc : neighbors) {
            int dist = distance(loc, burrowLoc);
            if (dist < bestDistance) {
                bestDistance = dist;
                bestMove = loc;
            }
        }

        //flyt kaninen hvis vi fandt et bedre felt
        if (bestMove != null) {
            world.move(this, bestMove);
            energy -= 10; // bevægelse koster energi, hvad skal hver bevægelse koste, synes i?
        }
    }

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

    public Burrow getBurrow() {return burrow;} //til test
    private int distance(Location a, Location b) {return Math.abs(a.getY() - b.getY()) + Math.abs(a.getX() - b.getX());}


    // ny setter – til test og evt. brug i programmet
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
}
