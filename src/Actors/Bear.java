package Actors;
import Inanimate.Bush;
import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.Set;

/**
 * Bear is a large predator that defends a specific territory.
 * It hunts various prey, seeks shelter at night, and reproduces under certain conditions.
 */
public class Bear extends Predator {
    Location territoryCenter;

    /**
     * Constructs a Bear with a specified territory center.
     *
     * @param territoryCenter the central location of the bear's territory
     */
    public Bear(Location territoryCenter) {
        super(400);
        this.territoryCenter = territoryCenter;
        this.shelter = territoryCenter;
    }

    // ----------- ACT -----------

    /**
     * Defines the actions of the bear in each simulation step.
     * The bear will seek shelter at night, hunt during the day,
     * and reproduce if conditions are met.
     *
     * @param world the world in which the bear acts
     */
    @Override
    public void act(World world) {
        if (!isAlive) return;
        super.act(world);
    }

    @Override
    public void nightBehaviour(World world) {
        reproduce(world);
        sleep(world);
    }

    @Override
    public void dayBehaviour(World world) {
        Location bearLocation = world.getLocation(this);
        if (!isInsideTerritory(bearLocation)) {
            moveTowardTerritory(world);
        } else if (isHungry()) {
            hunt(world);

        } else {
            moveRandomly(world);
        }
    }


    /**
     * Moves the bear one step toward the center of its territory.
     *
     * @param world the world in which the bear moves
     */
    private void moveTowardTerritory(World world) {
        moveOneStepTowards(world, territoryCenter, 5);
    }

    // ----------- LIFE -----------

    /**
     * Handles the bear's sleeping behavior.
     * The bear will sleep at its territory center if it is there or very close,
     * otherwise it will attempt to move toward the center.
     *
     * @param world the world in which the bear sleeps
     */
    @Override
    protected void handleSleepLocation(World world) {
        Location bearLoc = world.getLocation(this);

        if (bearLoc.equals(territoryCenter) || distance(bearLoc, territoryCenter) <= 1) {
            isSleeping = true;
            energy += 50;
        } else {
            Location center = territoryCenter;
            if (world.isTileEmpty(center)) {
                moveOneStepTowards(world, center);
            } else {
                Set<Location> neighbors = world.getEmptySurroundingTiles(center);
                if (!neighbors.isEmpty()) {
                    Location spot = neighbors.iterator().next();
                    moveOneStepTowards(world, spot);
                }
            }
        }
    }

    // ----------- HUNTING -----------

    /**
     * Implements the hunting behavior of the bear.
     * The bear prioritizes hunting enemy predators, scavenging carcasses,
     * eating berries from bushes, and hunting live prey in that order.
     *
     * @param world the world in which the bear hunts
     */
    @Override
    public void hunt(World world) {

        Location myLoc = world.getLocation(this);

        for (Location loc : world.getSurroundingTiles(myLoc)) {
            Object o = world.getTile(loc);
            if (o instanceof Herbivore h) {
                kill(world, h);
                return;
            }
        }

        Set<Location> area = getHuntingArea(world);  // dit territorie

        Location enemyLoc = findClosestEnemyPredator(world, area, myLoc);
        if (enemyLoc != null) {
            engageTarget(world, enemyLoc);
            return;
        }

        Location carcassLoc = findClosestCarcass(world, area, myLoc);
        if (carcassLoc != null) {
            engageTarget(world, carcassLoc);
            return;
        }

        Location bushLoc = findClosestBushWithBerries(world, area, myLoc);
        if (bushLoc != null) {
            engageTarget(world, bushLoc);
            return;
        }

        Location preyLoc = findClosestPrey(world, area, myLoc);
        if (preyLoc != null) {
            engageTarget(world, preyLoc);
        }
    }

    // ----------- EATING -----------

    /** Finds the closest bush with berries within a specified area.
     *
     * @param world the world in which to search
     * @param area the set of locations defining the search area
     * @param from the starting location for the search
     * @return the location of the closest bush with berries, or null if none found
     */
    private Location findClosestBushWithBerries(World world, Set<Location> area, Location from) {
        Location closest = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Location loc : area) {
            Object obj = world.getNonBlocking(loc);
            if (obj instanceof Bush bush) {
                if (bush.hasBerries()) {
                    int d = distance(from, loc);
                    if (d < bestDistance) {
                        bestDistance = d;
                        closest = loc;
                    }
                }
            }
        }
        return closest;
    }

    /** Checks if the bear can eat a given object.
     *
     * @param object the object to check
     * @return true if the object is a Carcass or Bush with berries, false otherwise
     */
    @Override
    public boolean canEat(Object object) {
        if (object instanceof Carcass) return true;
        if (object instanceof Bush bush) return bush.hasBerries();
        return false;
    }

    /**
     * Handles the eating behavior of the bear.
     * The bear can eat from carcasses and bushes with berries.
     *
     * @param world the world in which the bear eats
     * @param targetLoc the location of the food source
     */
    @Override
    public void eat(World world, Location targetLoc) {
        Object o = world.getTile(targetLoc);
        Object nb = world.getNonBlocking(targetLoc);

        if (o instanceof Carcass carcass) {
            int amount = Math.min(30, carcass.getMeatLeft());
            carcass.eaten(amount);
            energy += amount;

        } else if (nb instanceof Bush bush) {
            if (bush.hasBerries()) {
                int berries = bush.getBerryCount();
                bush.berriesEaten();
                energy += berries * 2;
            }
        }
    }

    /**
     * Returns the food energy provided by a given object.
     *
     * @param object the object to evaluate
     * @return the food energy value
     */
    @Override
    public int getFoodEnergy(Object object) {
        if (object instanceof Rabbit) return 40;
        if (object instanceof Wolf) return 60;
        if (object instanceof Bush bush && bush.hasBerries()) {
            return bush.getBerryCount()*2;
        }
        return 0;
    }

    /** Returns the meat value of the bear when it dies.
     *
     * @return the meat value
     */
    protected int getMeatValue() {
        return 100;
    }

    // ----------- REPRODUCTION -----------

    @Override
    public void reproduce(World world) {
        Location myLoc = world.getLocation(this);
        if (myLoc == null) return;

        Set<Location> territoryTiles = getHuntingArea(world);

        Bear mate = null;
        for (Location loc : territoryTiles) {
            Object obj = world.getTile(loc);
            if (obj instanceof Bear otherBear && otherBear != this && !otherBear.isChild()) {
                mate = otherBear;
                break;
            }
        }

        if (mate != null) {
            super.reproduce(world);
            // opdater begge bjørnes "amountOfKids"
            this.incrementKids();
            mate.incrementKids();
        }
    }

    private void incrementKids() {
        amountOfKids++;
    }

    /**
     * Creates a new bear child at the specified location.
     *
     * @param world the world in which the child is created
     * @param childLoc the location for the new child
     * @return the newly created bear child
     */
    @Override
    protected Animal createChild(World world, Location childLoc) {
        return new Bear(territoryCenter); // opretter en ny bjørn med sit territorie
    }

    /**
     * Determines the location for bear reproduction.
     * The bear prefers to place its offspring 2-3 tiles away from itself.
     * If no suitable location is found, it will try its shelter or nearby tiles.
     *
     * @param world the world in which reproduction occurs
     * @return the location for the new offspring, or null if no suitable location is found
     */
    @Override
    protected Location getReproductionLocation(World world) {
        Location parentLoc = world.getLocation(this);
        if (parentLoc == null) return null;

        int minDistance = 3;
        int maxDistance = 5;

        Set<Location> candidates = world.getSurroundingTiles(parentLoc, 5);
        for (Location loc : candidates) {
            int d = distance(parentLoc, loc);
            if (d >= minDistance && d <= maxDistance && world.isTileEmpty(loc)) {
                return loc;
            }
        }

        Location shelterLocation = territoryCenter;
        if (world.isTileEmpty(shelterLocation)) {
            return shelterLocation;
        }

        Set<Location> emptyAround = world.getEmptySurroundingTiles(shelterLocation);
        if (!emptyAround.isEmpty()) {
            return emptyAround.iterator().next();
        }
        return null;
    }

    // ----------- TERRITORY & FIGHT -----------

    /** Checks if a given location is inside the bear's territory.
     *
     * @param bearLocation the location to check
     * @return true if the location is inside the territory, false otherwise
     */
    public boolean isInsideTerritory(Location bearLocation) {
        if (bearLocation == null) {return false;}
        int radius = getRadius();
        return distance(bearLocation, territoryCenter) <= radius;
    }

    /** Determines if another animal is an enemy predator.
     *
     * @param other the other animal to evaluate
     * @return true if the other animal is an enemy predator, false otherwise
     */
    @Override
    public boolean isEnemyPredator(Animal other) {
        if (other instanceof Wolf) {
            return true;
        }
        return false;
    }

    // ----------- EXTRA/SETTERS/GETTERS/HELPERS/VISUAL -----------

    /** Returns the attack damage of the bear.
     *
     * @return the attack damage value
     */
    @Override
    public int getAttackDamage() {
        return 25;
    }

    /** Returns the movement cost for hunting.
     *
     * @return the hunt move cost
     */
    @Override
    protected int getHuntMoveCost() {
        return 8;
    }

    /** Returns the hunting area of the bear, which is its territory.
     *
     * @param world the world in which the bear hunts
     * @return the set of locations defining the hunting area
     */
    @Override
    protected Set<Location> getHuntingArea(World world) {
        return getTerritoryTiles(world);
    }

    /** Returns the radius of the bear's territory.
     *
     * @return the territory radius
     */
    private int getRadius() {
        return 3;
    }

    /** Returns the set of tiles that make up the bear's territory.
     *
     * @param world the world in which the territory exists
     * @return the set of territory locations
     */
    private Set<Location> getTerritoryTiles(World world) {
        int radius = getRadius();
        return world.getSurroundingTiles(territoryCenter, radius);
    }

    /**
     * Returns the display information for the bear, including color and image,
     * based on its age and sleeping state.
     *
     * @return the display information
     */
    @Override
    public DisplayInformation getInformation() {
        if (isChild()) {
            if (isSleeping) {
                return new DisplayInformation(Color.LIGHT_GRAY, "bear-small-sleeping");
            } else {
                return new DisplayInformation(Color.LIGHT_GRAY, "bear-small");
            }
        } else {
            if (isSleeping) {
                return new DisplayInformation(Color.GRAY, "bear-sleeping");
            } else {
                return new DisplayInformation(Color.GRAY, "bear");
            }
        }
    }
}
