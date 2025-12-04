import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.Set;

public class Bear extends Predator {
    Location territoryCenter;

    public Bear(Location territoryCenter) {
        super(); // kalder Animal's constructor
        this.territoryCenter = territoryCenter;
    }

    @Override
    public void act(World world) {
        super.tickCommon(world);

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
                    if (isHungry()) {
                        hunt(world);
                    } else {
                        moveRandomly(world);
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

    @Override
    public void eat(World world, Location targetLoc) {
        Object o = world.getTile(targetLoc);

        if (o instanceof Carcass carcass) {
            int amount = Math.min(30, carcass.getMeatLeft()); // bjørn kan spise mere pr. bid end ulv
            carcass.eaten(amount);
            energy += amount;

        } else if (o instanceof Bush bush) {
            if (bush.hasBerries()) {
                int berries = bush.getBerryCount();
                bush.berriesEaten();   // alle bær væk
                energy += berries * 2;
            }
        }
    }

    @Override
    protected void hunt(World world) {
        Location myLoc = world.getLocation(this);
        Set<Location> area = getHuntingArea(world);  // dit territorie

        // 1) PRIORITET: fjendtligt rovdyr
        Location enemyLoc = findClosestEnemyPredator(world, area, myLoc);
        if (enemyLoc != null) {
            engageTarget(world, enemyLoc);
            return;
        }

        // 2) PRIORITET: ådsel (Carcass), men kun hvis sulten
        if (isHungry()) {
            Location carcassLoc = findClosestCarcass(world, area, myLoc);
            if (carcassLoc != null) {
                engageTarget(world, carcassLoc);
                return;
            }

            // 3) PRIORITET: busk med bær (stadig kun hvis sulten)
            Location bushLoc = findClosestBushWithBerries(world, area, myLoc);
            if (bushLoc != null) {
                engageTarget(world, bushLoc);
                return;
            }
        }

        // 4) PRIORITET: levende bytte (Rabbit m.m.)
        Location preyLoc = findClosestPrey(world, area, myLoc);
        if (preyLoc != null) {
            engageTarget(world, preyLoc);
        }
        // ellers: ingen mål → gør ikke mere i denne tur
    }



    @Override
    protected boolean canEat(Object object) {
        return object instanceof Carcass || object instanceof Bush;
    }


    @Override
    protected int getFoodEnergy(Object object) {
        if (object instanceof Rabbit) return 40;
        if (object instanceof Wolf) return 60;
        if (object instanceof Bush) return 10; // pr. bær
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
        return new Bear(territoryCenter); // opretter en ny bjørn med sit territorie
    }

    @Override
    protected Location getReproductionLocation(World world) {
        Location parentLoc = world.getLocation(this);
        if (parentLoc == null) return null;

        // Vi vil have ungen 2-3 felter væk fra forælderen
        int minDistance = 3;
        int maxDistance = 5;

        Set<Location> candidates = world.getSurroundingTiles(parentLoc, 5); // alle felter opmrking parent
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


    private int getRadius() {
        return 3;
    }

    private Set<Location> getTerritoryTiles(World world) {
        int radius = getRadius();
        return world.getSurroundingTiles(territoryCenter, radius);
    }

    private boolean isInsideTerritory(Location bearLocation) {
        if (bearLocation == null) {return false;}
        int radius = getRadius();
        return distance(bearLocation, territoryCenter) <= radius;
    }

    private Location findClosestBushWithBerries(World world, Set<Location> area, Location from) {
        // bruger Predators findClosestMatching(...)
        return findClosestMatching(world, area, from, o ->
                o instanceof Bush bush && bush.hasBerries()
        );
    }


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

    @Override
    protected Set<Location> getHuntingArea(World world) {
        return getTerritoryTiles(world);
    }


    @Override
    protected int getHuntMoveCost() {
        return 8;
    }

    @Override
    protected boolean isEnemyPredator(Animal other) {
        if (!(other instanceof Predator)) return false;

        if (other instanceof Wolf) {
            return true;  // bjørn ser ulve som konkurrenter
        }

        if (other instanceof Bear otherBear) {
            // evt. ikke fjender: return false;
            return otherBear != this; // aldrig “fjende med sig selv”
        }

        return false;
    }


    @Override
    protected int getAttackDamage() {
        return 25;
    }
}
