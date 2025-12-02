import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.Set;

public class Bear extends Animal {
    Location territoryCenter;

    public Bear(Location territoryCenter) {
        super(); // kalder Animal's constructor
        this.territoryCenter = territoryCenter;
    }

    @Override
    public void act(World world) {
        super.act(world);

        if (getAge() >= 400 || getEnergy() <= 0) {
            die(world);
            return;
        }

        if (isAlive) {
            if (world.getCurrentTime() >= World.getDayDuration() - 3) {
                seekShelter(world);

            } else if (world.isDay()) {
                Location bearLocation = world.getLocation(this);
                if (!isInsideTerritory(bearLocation)) {
                    moveTowardTerritory(world);
                } else {
                    if (getEnergy() <= 50) {
                        forageOrHunt(world); // tjek både dyr og bær
                    } else {
                        super.moveRandomly(world);
                    }
                }

            } else if (world.isNight()) {
                sleep(world);
            }
        }
    }

    private void forageOrHunt(World world) {
        // 1. Find alle felter i bjørnens territorium
        Set<Location> territoryTiles = getTerritoryTiles(world);

        // 2. Gå dem igennem og find enten bytte eller buske med bær
        for (Location tile : territoryTiles) {
            Object o = world.getTile(tile);

            if (o == null) continue;
            if (o == this) continue;

            // CASE A: dyr (levende eller carcass)
            if (o instanceof Animal prey && canEat(prey)) {
                Location bearLoc = world.getLocation(this);
                Set<Location> neighbors = world.getSurroundingTiles(bearLoc);

                if (neighbors.contains(tile)) {
                    // vi står ved siden af → spis
                    eat(world, tile);
                } else {
                    // ellers bevæg os tættere på byttet
                    moveOneStepTowardsPrey(world, tile);
                }
                return; // stop efter første fund
            }

            // CASE B: busk med bær
            if (o instanceof Bush bush && bush.hasBerries()) {
                Location bearLoc = world.getLocation(this);
                Set<Location> neighbors = world.getSurroundingTiles(bearLoc);

                if (neighbors.contains(tile)) {
                    eat(world, tile);
                } else {
                    // ellers bevæg os tættere på busken
                    moveOneStepTowards(world, tile, 5); // energikost fx 5
                }
                return; // stop efter første fund
            }
        }
    }

    private void moveTowardTerritory(World world) {
        moveOneStepTowards(world, territoryCenter, 5);
    }

    private void moveOneStepTowardsPrey(World world, Location preyLoc) {
        moveOneStepTowards(world, preyLoc, 8);
    }

    @Override
    public void eat(World world, Location targetLoc) {
        Object o = world.getTile(targetLoc);

        if (o instanceof Bush bush) {
            if (bush.hasBerries()) {
                int berries = bush.getBerryCount();
                bush.berriesEaten();   // reducerer antallet af bær, men busken bliver stående
                energy += berries;     // bjørnen får energi fra bærene
            }
        } else if (o instanceof Animal prey && canEat(prey)) {
            if (!prey.isAlive()) {
                // spiser carcass → fjern dyret helt
                world.delete(prey);
            } else {
                // spiser levende bytte
                prey.die(world);
                world.delete(prey);
            }
            energy += getFoodEnergy(prey);
        }
    }

    @Override
    protected boolean canEat(Object object) {
        return object instanceof Rabbit || object instanceof Wolf;
    }

    @Override
    protected int getFoodEnergy(Object object) {
        if (object instanceof Rabbit) {
            return 40;
        } else  if (object instanceof Wolf) {
            return 60;
        }
        return 0;
    }

    @Override
    protected void handleSleepLocation(World world) {
        Location bearLoc = world.getLocation(this);

        if (bearLoc.equals(territoryCenter) || distance(bearLoc, territoryCenter) <= 1) {
            isSleeping = true;
            energy += 50;
            return;
        } else {
            // prøv at gå mod centeret
            Location center = territoryCenter;
            if (world.isTileEmpty(center)) {
                moveOneStepTowards(world, center, 5);
            } else { // center optaget → find et tomt nabofelt
                Set<Location> neighbors = world.getEmptySurroundingTiles(center);
                if (!neighbors.isEmpty()) {
                    Location spot = neighbors.iterator().next();
                    moveOneStepTowards(world, spot, 5);
                }
            }
        }
    }

    @Override
    protected Animal createChild() {
        return null;
    }

    @Override
    protected Location getReproductionLocation(World world) {
        return null;
    }

    private int getRadius() {
        return 3;
    }

    private Set<Location> getTerritoryTiles(World world) {
        // radius 2
        int radius = getRadius();
        return world.getSurroundingTiles(territoryCenter, radius);
    }

    private boolean isInsideTerritory(Location bearLocation) {
        if (bearLocation == null) {return false;}
        int radius = getRadius();
        return distance(bearLocation, territoryCenter) <= radius;
    }

    private boolean isPreyInsideTerritory(World world) {
        Set<Location> bearTerritory = getTerritoryTiles(world);

        Set<Rabbit> rabbitsInBearTerritory = world.getAll(Rabbit.class, bearTerritory);
        Set<Wolf> wolvesInBearTerritory = world.getAll(Wolf.class, bearTerritory);

        return !rabbitsInBearTerritory.isEmpty() || !wolvesInBearTerritory.isEmpty();
    }

    @Override
    public DisplayInformation getInformation() {
        if (getAge() < 60) {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "bear-small-sleeping");
            } else if (!isAlive) {
                return new DisplayInformation(Color.GRAY, "carcass-small");
            } else {
                return new DisplayInformation(Color.GRAY, "bear-small"); // billede af bjørneunge
            }
        } else {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "bear-sleeping");
            } else if (!isAlive) {
                return new DisplayInformation(Color.GRAY, "carcass");
            } else {
                return new DisplayInformation(Color.DARK_GRAY, "bear"); // billede af voksen bjørn
            }
        }
    }
}
