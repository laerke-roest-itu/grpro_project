import Actors.Bear;
import Actors.Carcass;
import Inanimate.Fungi;
import Inanimate.Grass;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The CarcassTest class verifies the core behavioral logic of the {@link Carcass} actor
 * within a simulated {@link World}.
 * The tests ensure that a Carcass interacts correctly with its environment,
 * including reducing its meat level when eaten, rotting over time, and spawning
 * {@link Fungi} when infected and decaying.
 */
class CarcassTest {

    private Program program;
    private World world;

    private Location loc;

    /**
     * Helper class to provide a fixed random value.
     */
    static class FixedRandom extends Random {
        private final double value;
        FixedRandom(double value) { this.value = value; }
        @Override public double nextDouble() { return value; }
    }

    /**
     * Sets up the world and test environment before each test.
     */
    @BeforeEach
    void setUp() {
        program = new Program(10, 500, 0);
        world = program.getWorld();
        world.setDay();
        loc = new Location(5, 5);
    }

    /**
     * Cleans up the test environment after each test.
     */
    @AfterEach
    void tearDown() {
        program = null;
        world = null;
        loc = null;
    }

    /**
     * Test that eating from a carcass reduces its meat level and that it doesn't go below zero.
     */
    @Test
    void eatenReducesMeatLeft_andClampsAtZero() {
        Carcass carcass = new Carcass(50, 10);
        world.setTile(loc, carcass);
        Bear bear = new Bear(new Location(5, 6));
        world.setTile(new Location(5, 6), bear);

        bear.setEnergy(10);
        int before = carcass.getMeatLeft();

        bear.act(world);
        assertTrue(carcass.getMeatLeft() < before);

        carcass.eaten(999);
        assertEquals(0, carcass.getMeatLeft(), "Meat should not go below 0");
    }

    /**
     * Test that a carcass is deleted when its rot timer reaches zero.
     */
    @Test
    void actDeletesCarcassWhenRotTimerRunsOut() {
        Carcass carcass = new Carcass(10, 1);
        world.setTile(loc, carcass);

        carcass.act(world);

        assertFalse(world.contains(carcass), "Carcass should be deleted from world when rotTimer <= 0");
        assertNull(world.getTile(loc), "Tile should be empty after carcass disappears (no fungi)");
    }

    /**
     * Test that a carcass is deleted when all meat has been eaten.
     */
    @Test
    void actDeletesCarcassWhenMeatRunsOut() {
        Carcass carcass = new Carcass(1, 10);
        world.setTile(loc, carcass);

        carcass.eaten(1); // meatLeft = 0
        carcass.act(world);

        assertFalse(world.contains(carcass), "Carcass should be deleted when meatLeft <= 0");
    }

    /**
     * Test that a carcass infected with fungi spawns a Fungi object when it rots away.
     */
    @Test
    void carcassWithFungiSpawnsFungiWhenItDisappears() {
        Carcass carcass = new Carcass(10, 1, true);
        world.setTile(loc, carcass);

        carcass.act(world);

        assertFalse(world.contains(carcass));

        Object nb = world.getNonBlocking(loc);
        assertTrue(nb instanceof Fungi, "Fungi should be spawned on the tile when carcass had fungi");
    }

    /**
     * Test that fungi spawned from a rotting carcass replaces existing grass on the tile.
     */
    @Test
    void carcassWithFungiOverwritesGrassWhenItDisappears() {
        world.setTile(loc, new Grass());

        Location carcassLoc = new Location(5, 6);
        Carcass carcass = new Carcass(10, 1, true);
        world.setTile(carcassLoc, carcass);

        world.delete(world.getNonBlocking(loc));
        world.setTile(carcassLoc, new Grass());

        carcass.act(world);

        Object nb = world.getNonBlocking(carcassLoc);
        assertTrue(nb instanceof Fungi, "Fungi should overwrite Grass when carcass disappears");
    }

    /**
     * Test that a carcass can become infected with fungi over time.
     */
    @Test
    void carcassCanBecomeInfectedViaRandom() {
        Carcass carcass = new Carcass(10, 2, false, new FixedRandom(0.0));
        world.setTile(loc, carcass);

        carcass.act(world);

        carcass.act(world);

        assertFalse(world.contains(carcass), "Carcass should rot faster after being infected");
    }
}

