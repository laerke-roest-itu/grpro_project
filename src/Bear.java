import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;

import java.util.HashSet;
import java.util.Set;

public class Bear extends Animal implements Actor {
    Location territoryCenter;

    public Bear(Location territoryCenter) {
        super(); // kalder Animal's constructor
        this.territoryCenter = territoryCenter;
    }

    @Override
    public void act(World world) {
        Location bearLocation = world.getLocation(this);

        super.tickCommon(world);

        if (!isInsideTerritory(bearLocation)) {
            moveTowardTerritory(world);
        } else if (isPreyInsideTerritory(world)) {
            hunt(world);
        } else if (eatBerriesIfHungry(world)) {
        } else {
            super.moveRandomly(world);
        }
    }

    private void hunt(World world) {
        // 1. Find alle felter i bjørnens territorium
        Set<Location> territoryTiles = getTerritoryTiles(world);

        // 2. Gå dem igennem og find et byttedyr
        for (Location tile : territoryTiles) {
            Object o = world.getTile(tile);

            // spring tomme felter over
            if (o == null) continue;

            // vi interesserer os kun for dyr
            if (!(o instanceof Animal)) continue;

            // lad være med at spise os selv
            if (o == this) continue;

            // tjek om vi MÅ spise det her dyr (Rabbit/Wolf styres af canEat)
            if (!canEat(o)) continue;

            // NU har vi fundet et bytte inde i territoriet
            Location bearLoc = world.getLocation(this);

            // 3. Er bjørnen allerede nabo til byttet?
            Set<Location> neighbors = world.getSurroundingTiles(bearLoc);
            if (neighbors.contains(tile)) {
                // vi står ved siden af → spis
                eat(world, tile);
            } else {
                // 4. Ellers: bevæg os ét skridt tættere på byttet
                // energyCost kan vi selv vælge (fx 10)
                moveOneStepTowardsPrey(world, tile);
            }

            return;
        }
    }


    private void moveTowardTerritory(World world) {
        moveOneStepTowards(world, territoryCenter, 5);
    }

    private void moveOneStepTowardsPrey(World world, Location preyLoc) {
        moveOneStepTowards(world, preyLoc, 8);
    }

    private boolean eatBerriesIfHungry(World world) {
        if (getEnergy() < 50) {
            Location bearLoc = world.getLocation(this);
            Set<Location> bearNeighborTiles = world.getSurroundingTiles(bearLoc);

            for (Location bushLoc : bearNeighborTiles) {
                if (world.containsNonBlocking(bushLoc)) {
                    Object possibleBush = world.getNonBlocking(bushLoc);
                    if (possibleBush instanceof Bush) {
                        if (((Bush) possibleBush).hasBerries()) {
                            int berries = ((Bush) possibleBush).getBerryCount();
                            ((Bush) possibleBush).berriesEaten();
                            energy += berries;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void eat(World world, Location targetLoc) {
        Object o = world.getTile(targetLoc);
        if (o != null && canEat(o)) {
            world.delete(o);
            energy += getFoodEnergy(o);
        }
    }


    @Override
    protected boolean canEat(Object object) {
        return object instanceof Rabbit || object instanceof Wolf;
    }

    @Override
    protected int getFoodEnergy(Object object) {
        return 40; // eller forskelligt pr. type hvis du vil
    }


    @Override
    protected void handleSleepLocation(World world) {

    }

    @Override
    protected Animal createChild() {
        return null;
    }

    @Override
    protected Location getReproductionLocation(World world) {
        return null;
    }

    private Set<Location> getTerritoryTiles(World world) {
        // radius 2
        int radius = 2;
        return world.getSurroundingTiles(territoryCenter, radius);
    }

    private boolean isInsideTerritory(Location bearLocation) {
        if (bearLocation == null) {return false;}
        int radius = 2; // samme som i getTerritoryTiles
        return distance(bearLocation, territoryCenter) <= radius;
    }

    private boolean isPreyInsideTerritory(World world) {
        Set<Location> bearTerritory = getTerritoryTiles(world);

        Set<Rabbit> rabbitsInBearTerritory = world.getAll(Rabbit.class, bearTerritory);
        Set<Wolf> wolvesInBearTerritory = world.getAll(Wolf.class, bearTerritory);

        return !rabbitsInBearTerritory.isEmpty() || !wolvesInBearTerritory.isEmpty();
    }
}