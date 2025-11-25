import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import itumulator.world.World;
import itumulator.world.Location;

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
        assertInstanceOf(Burrow.class, obj, "Rabbit should dig a burrow at its location");
        assertNotNull(rabbit.getBurrow(), "Rabbit should remember its burrow");
    }

    @Test
    void rabbitShouldDieWhenEnergyIsZero() {
        rabbit.setEnergy(0); //sætter energy til 0

        Set<Location> neighbours = world.getSurroundingTiles(startLoc);

        rabbit.act(world);

        assertNull(world.getTile(startLoc), "Rabbit should be removed from world when dead");
        for (Location n : neighbours) {
            assertTrue(world.isTileEmpty(n));
        }
    }

    @Test
    void rabbitShouldSeekBurrowAtDusk() {
        // Lav et burrow et par felter væk
        Burrow burrow = new Burrow();
        Location burrowLoc = new Location(7, 5);
        world.setTile(burrowLoc, burrow);
        rabbit.claimBurrow(world);

        // Simuler skumring
        world.setCurrentTime(World.getDayDuration() - 3);

        rabbit.act(world);

        Location newLoc = world.getLocation(rabbit);
        assertNotEquals(startLoc, newLoc, "Rabbit should move towards burrow at dusk");
        assertEquals(5, newLoc.getX(), "Rabbit should stay in same column (col=5)");
        assertEquals(6, newLoc.getY(), "Rabbit should move one row closer to burrow (row=6)");

    }

    @Test
    void rabbitShouldReproduceInBurrow() {
        // Placér et burrow på kaninens lokation og claim det
        Burrow burrow = new Burrow();
        world.setTile(startLoc, burrow);
        rabbit.claimBurrow(world);

        // Simuler nat
        world.setNight();

        rabbit.reproduce(world);

        Object obj = world.getTile(startLoc);
        assertTrue(obj instanceof Burrow || obj instanceof Rabbit,
                "Burrow should contain either itself or a new Rabbit child");
    }
}
