import Inanimate.Fungi;
import Actors.Carcass;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The FungiTest class verifies the core behavioral logic of the {@link Fungi} actor
 * within a simulated {@link World}.
 * The tests ensure that a Fungi interacts correctly with its environment,
 * including decreasing lifespan per tick, deleting itself when lifespan reaches zero,
 * and infecting {@link Carcass} objects within its infection radius.
 */
class FungiTest {

    private Program program;
    private World world;

    private Fungi fungi;
    private Location fungiLoc;

    /**
     * Sets up the world and test environment before each test.
     */
    @BeforeEach
    void setUp() {
        program = new Program(12, 500, 0);
        world = program.getWorld();

        world.setDay();
        world.setCurrentTime(0);

        fungiLoc = new Location(5, 5);
        fungi = new Fungi(3);
        world.setTile(fungiLoc, fungi);
    }

    /**
     * Cleans up the test environment after each test.
     */
    @AfterEach
    void tearDown() {
        program = null;
        world = null;
        fungi = null;
    }

    /**
     * Test that fungi lifespan decreases each time it acts.
     */
    @Test
    void fungiLifespanDecreasesEachAct() {
        int before = fungi.getLifespan();
        fungi.act(world);
        assertEquals(before - 1, fungi.getLifespan());
    }

    /**
     * Test that fungi deletes itself when its lifespan reaches zero.
     */
    @Test
    void fungiDeletesItselfWhenLifespanHitsZero() {
        fungi.act(world);
        fungi.act(world);
        fungi.act(world);

        assertFalse(world.contains(fungi), "Fungi should be deleted from world when lifespan <= 0");
    }

    /**
     * Test that fungi can infect a carcass within its infection radius.
     */
    @Test
    void fungiInfectsCarcassWithinRadiusTwo() {
        Location carcassLoc = new Location(7, 5);
        Carcass carcass = new Carcass(50, 10);
        world.setTile(carcassLoc, carcass);

        fungi.act(world);

        int ticks = 0;
        while (world.contains(carcass) && ticks < 10) {
            carcass.act(world);
            ticks++;
        }

        assertFalse(world.contains(carcass),
                "Carcass should rot away within 10 ticks after fungi infection (faster rot)");
        assertTrue(ticks <= 6,
                "Carcass should rot faster when infected (expected around 5 ticks, got " + ticks + ")");
    }

    /**
     * Test that fungi does not infect a carcass outside its infection radius.
     */
    @Test
    void fungiDoesNotInfectCarcassOutsideRadiusTwo() {
        // fungi på (5,5)
        // inde i radius 2: (7,5)
        Location insideLoc = new Location(7, 5);
        Carcass inside = new Carcass(50, 10);
        world.setTile(insideLoc, inside);

        // udenfor radius 2: (8,5) (distance 3)
        Location outsideLoc = new Location(8, 5);
        Carcass outside = new Carcass(50, 10);
        world.setTile(outsideLoc, outside);

        // kør fungi én gang så den kan inficere "inside"
        fungi.act(world);

        int insideTicks = 0;
        while (world.contains(inside) && insideTicks < 20) {
            inside.act(world);
            insideTicks++;
        }

        int outsideTicks = 0;
        while (world.contains(outside) && outsideTicks < 20) {
            outside.act(world);
            outsideTicks++;
        }

        assertTrue(insideTicks < outsideTicks,
                "Carcass inside radius should rot faster than carcass outside radius. " +
                        "inside=" + insideTicks + ", outside=" + outsideTicks);
    }

}
