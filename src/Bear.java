import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;

import java.util.Set;

public class Bear extends Animal implements Actor {
    Location territoryCenter;

    public Bear(Location territoryCenter) {
        this.territoryCenter = territoryCenter;
    }

    @Override
    public void act(World world) {
        Location bearLocation = world.getLocation(this);
        Set<Location> emptyTilesNearBear = world.getEmptySurroundingTiles(bearLocation);
        if (!emptyTilesNearBear.contains(territoryCenter)) {
            moveTowardTerritory(world);
            age++;
            energy --;
        } else {
            super.act(world);
        }
    }

    private void hunt(World world) {
        Set<Location> BearTerritory = getTerritoryTiles(world);
        for (Location BearTerritoryTile: BearTerritory) {
            Object o = world.getTile(BearTerritoryTile);

            // spring tomme felter over
            if (o == null) continue;

            // vi interesserer os kun for dyr
            if (!(o instanceof Animal)) continue;

            // lad være med at spise os selv
            if (o == this) continue;

            if (canEat(o)) {
                // NU har vi fundet et bytte inde i territoriet
                // → vi vil hen til et nabofelt ved 'tile'.

                Location bearLoc = world.getLocation(this);

                // er vi allerede nabo til byttet?
                Set<Location> neighbors = world.getSurroundingTiles(bearLoc);
                if (neighbors.contains(BearTerritoryTile)) {
                    // vi står ved siden af → spis
                    eat(world, BearTerritoryTile);
                } else {
                    // ellers: bevæg os ét skridt tættere på 'tile'
                    moveOneStepTowards(world, BearTerritoryTile);
                }
            }
        }

    }

    private void moveOneStepTowards(World world, Location bearTerritoryTile) {
        Location bearLoc = world.getLocation(this);

        Set<Location> emptyNeighbors =
                world.getEmptySurroundingTiles(bearLoc);

        Location bestMove = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Location loc : emptyNeighbors) {
            int d = distance(loc, bearTerritoryTile);
            if (d < bestDistance) {
                bestDistance = d;
                bestMove = loc;
            }
        }

        if (bestMove != null) {
            world.move(this, bestMove);
        }
    }

    private void moveTowardTerritory(World world) {
        Location bearLoc = world.getLocation(this);

        Set<Location> emptyNeighbors =
                world.getEmptySurroundingTiles(bearLoc);

        Location bestMove = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Location loc : emptyNeighbors) {
            int d = distance(loc, territoryCenter);
            if (d < bestDistance) {
                bestDistance = d;
                bestMove = loc;
            }
        }

        if (bestMove != null) {
            world.move(this, bestMove);
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



    private int distance(Location a, Location b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private Set<Location> getTerritoryTiles(World world) {
        // radius 2
        int radius = 2;
        return world.getSurroundingTiles(territoryCenter, radius);
    }



}
