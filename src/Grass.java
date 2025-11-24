import itumulator.simulator.Actor;
import itumulator.world.World;
import itumulator.world.Location;
import itumulator.world.NonBlocking;
import java.util.*;

public class Grass implements Actor, NonBlocking {
    Random random;
    @Override
    public void act(World world) {
        // Finder græssets nuværende position i verden
        Location grassLocation = world.getLocation(this);

        // Henter alle nabofelter
        Set<Location> neighbours = world.getSurroundingTiles(grassLocation);

        // Opretter en Random-generator
        random = new Random();

        // 5% chance for at græsset spreder sig i dette act
        if (random.nextInt(100) <= 5) {

            // Gennemgå alle nabofelter ét ad gangen
            for (Location neighbourTile : neighbours) {

                // Tjekker om der INGEN non-blocking objekter findes på dette felt
                // (dvs. der står ikke allerede græs eller noget andet non-blocking)
                if (world.containsNonBlocking(neighbourTile) == false) {

                    // Placér nyt græs på alle nabofelter
                    // hvis ikke der allerede er non-blocking objekter
                    world.setTile(neighbourTile, new Grass());
                }
            }
        }

    }
}