import Actors.Rabbit;
import Inanimate.Burrow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import itumulator.world.World;
import itumulator.world.Location;

import java.util.Random;
import java.util.Set;

public class RabbitTest {

    private World world;
    private Rabbit rabbit;
    private Location startLoc;

    @BeforeEach
    void setUp() {
        // Opret en lille testverden og en kanin på en fast lokation
        world = new World(10);
        rabbit = new Rabbit();
        startLoc = new Location(5, 5);
        world.setTile(startLoc, rabbit);
    }

    @AfterEach
    void tearDown() {
        world = null;
        rabbit = null;
        startLoc = null;
    }

    @Test
    void rabbitShouldDigBurrow() {
        rabbit.digBurrow(world);

        Object obj = world.getNonBlocking(startLoc);
        assertInstanceOf(Burrow.class, obj, "Actors.Rabbit should dig a burrow at its location");
        assertNotNull(rabbit.getBurrow(), "Actors.Rabbit should remember its burrow");
    }

    @Test
    void rabbitShouldDieWhenEnergyIsZero() {
        rabbit.setEnergy(0); //sætter energy til 0

        Set<Location> neighbours = world.getSurroundingTiles(startLoc);

        rabbit.act(world);

        assertNull(world.getTile(startLoc), "Actors.Rabbit should be removed from world when dead");
        for (Location n : neighbours) {
            assertTrue(world.isTileEmpty(n));
        }
    }

    @Test
    void rabbitShouldSeekBurrowAtDusk() {
        Burrow burrow = new Burrow();
        Location burrowLoc = new Location(7, 5);
        world.setTile(burrowLoc, burrow);

        // direkte give kaninen sit hul:
        rabbit.setBurrow(burrow);

        world.setCurrentTime(World.getDayDuration() - 3);

        rabbit.act(world);

        Location newLoc = world.getLocation(rabbit);
        Location expLoc = new Location(6,5);

        assertNotEquals(startLoc, newLoc, "Actors.Rabbit should move towards burrow at dusk");
        assertEquals(expLoc, newLoc, "Actors.Rabbit should move one step towards the burrow at dusk");
    }



    @Test
    void rabbitShouldReproduceInBurrow() {
        Random alwaysReproduce = new Random() {
            @Override
            public double nextDouble() {
                return 0.0; // tvinger Parring
            }
        };
        world.delete(rabbit);

        rabbit = new Rabbit(alwaysReproduce);
        world.setTile(startLoc, rabbit);
        // Placér et burrow på kaninens lokation og claim det
        Burrow burrow = new Burrow();
        world.setTile(startLoc, burrow);
        rabbit.setBurrow(burrow);

        // Simuler nat
        world.setNight();

        rabbit.reproduce(world);

        Object obj = world.getTile(startLoc);
        assertEquals(1, rabbit.getAmountOfKids(),
                "Inanimate.Burrow should contain a new Actors.Rabbit child");
    }
}
