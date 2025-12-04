package Actors;

import Inanimate.Bush;

import Inanimate.Carcass;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.Set;

public class Bear extends Animal {
    Location territoryCenter;

    public Bear(Location territoryCenter) {
        super(); // kalder Actors.Animal's constructor
        this.territoryCenter = territoryCenter;
        this.shelter = territoryCenter;
    }

    // ----------- ACT -----------

    @Override
    public void act(World world) {
        super.act(world);

        if (getAge() >= 400 || getEnergy() <= 0) {
            super.die(world);
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
                    if (getEnergy() <= 50 && isPreyInsideTerritory(world)) {
                        forageOrHunt(world); // tjek både dyr og bær
                    } else {
                        super.moveRandomly(world);
                    }
                }

            } else if (world.isNight()) {
                reproduce(world);
                sleep(world);
            }
        }
    }

    private void moveTowardTerritory(World world) {
        moveOneStepTowards(world, territoryCenter, 5);
    }

    private void moveOneStepTowardsPrey(World world, Location preyLoc) {
        moveOneStepTowards(world, preyLoc, 8);
    }

    // ----------- LIFE -----------

    @Override
    protected void handleSleepLocation(World world) {
        Location bearLoc = world.getLocation(this);

        if (bearLoc.equals(territoryCenter) || distance(bearLoc, territoryCenter) <= 1) {
            isSleeping = true;
            energy += 50;
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
    public void wakeUp(World world) { //SKAL TILPASSES
        isSleeping = false;
    }

    // ----------- EATING -----------

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

    @Override
    public void eat(World world, Location targetLoc) {
        Object o = world.getTile(targetLoc);

        if (o instanceof Bush bush) {
            if (bush.hasBerries()) {
                int berries = bush.getBerryCount();
                bush.berriesEaten();   // reducerer antallet af bær, men busken bliver stående
                energy += berries;     // bjørnen får energi fra bærrene
            }
        } else if (o instanceof Carcass carcass) {
            int amount = Math.min(30, carcass.getMeatLeft()); // bjørnen spiser mere end ulven
            carcass.eaten(amount);
            energy += amount;

        } else if (o instanceof Animal prey) {
            prey.die(world);
            // Hent carcass fra samme felt og spis det
            Object newObj = world.getTile(targetLoc);
            if (newObj instanceof Carcass newCarcass) {
                int amount = Math.min(30, newCarcass.getMeatLeft());
                newCarcass.eaten(amount);
                energy += amount;
            }
        }
    }

    @Override
    protected boolean canEat(Object object) {
        return object instanceof Carcass || object instanceof Bush;
    }

    protected int getFoodEnergy(Object object) {
        return 0;
    }

    // ----------- REPRODUCTION -----------

    @Override
    public void reproduce(World world) {
        Set<Bear> bearsInTerritory = world.getAll(Bear.class, getTerritoryTiles(world));
        if (bearsInTerritory.size() >= 5) return; // maks 5 bjørne i territoriet

        Location childLoc = getReproductionLocation(world);
        if (childLoc != null) {
            Bear child = new Bear(childLoc);
            world.setTile(childLoc, child);
            amountOfKids++;
            energy -= 15;
        }
    }

    @Override
    protected Location getReproductionLocation(World world) {
        Location parentLoc = world.getLocation(this);
        if (parentLoc == null) return null;

        // Vi vil have ungen 2-3 felter væk fra forælder
        int minDistance = 3;
        int maxDistance = 5;

        Set<Location> candidates = world.getSurroundingTiles(parentLoc, 5); // alle felter omkring parent
        for (Location loc : candidates) {
            int d = distance(parentLoc, loc);
            if (d >= minDistance && d <= maxDistance && world.isTileEmpty(loc)) {
                return loc; // vælg første der passer
            }
        }
        // fallback: hvis ingen fundet, prøv shelter eller naboer
        Location shelterLocation = territoryCenter;
        if (world.isTileEmpty(shelterLocation)) {
            return shelterLocation;
        }
        Set<Location> emptyAround = world.getEmptySurroundingTiles(shelterLocation);
        if (!emptyAround.isEmpty()) {
            return emptyAround.iterator().next();
        }
        return null; // ingen plads → ingen reproduktion
    }

    @Override
    protected Animal createChild(World world, Location childLoc) {
        return new Bear(childLoc); // ungen får sit eget territoriecenter
    }

    // ----------- TERRITORY -----------

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
        Set<Bush> bushesInBearTerritory = world.getAll(Bush.class, bearTerritory);

        return !rabbitsInBearTerritory.isEmpty() || !wolvesInBearTerritory.isEmpty() || !bushesInBearTerritory.isEmpty();
    }

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    @Override
    public DisplayInformation getInformation() {
        if (isChild()) {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "bear-small-sleeping");
            } else {
                return new DisplayInformation(Color.GRAY, "bear-small"); // billede af bjørneunge
            }
        } else {
            if (isSleeping) {
                return new DisplayInformation(Color.DARK_GRAY, "bear-sleeping");
            } else {
                return new DisplayInformation(Color.DARK_GRAY, "bear"); // billede af voksen bjørn
            }
        }
    }
}
