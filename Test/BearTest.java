import Actors.Bear;
import Actors.Wolf;
import Inanimate.Bush;
import Actors.Carcass;
import itumulator.world.Location;
import itumulator.world.World;
import itumulator.executable.Program;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The BearTest class verifies the core behavioral logic of the {@link Bear} actor
 * within a simulated {@link World}.
 *
 * The tests ensure that a Bear interacts correctly with its environment,
 * including movement relative to its territory center, energy consumption
 * during actions, and interactions with other entities such as
 * {@link Carcass}, {@link Bush}, and {@link Wolf}.
 *
 * The class focuses on validating observable effects of the Bear's
 * {@code act(World)} behavior rather than internal implementation details.
 */

class BearTest {

    World world;
    Bear bear;
    Location center;

    /**
     * The {@code @BeforeEach} and {@code @AfterEach} methods set up and tear down
     * the world state used by each Bear test as part of the JUnit test lifecycle.
     */

    @BeforeEach
    void setUp() {
        Program program = new Program(10, 500, 0);
        world = program.getWorld();

        center = new Location(5, 5);
        bear = new Bear(center);
        world.setTile(center, bear);
    }

    @AfterEach
    void tearDown() {
        world = null;
        bear = null;
    }

    /**
     * Test that a bear loses energy when acting and moves towards its assigned territory center
     * if it is currently outside of it.
     */
    @Test
    void bearLosesEnergyWhenActing_and_movesTowardsTerritoryCenter() {
        world.delete(bear);

        Location outsideOfTerritory = new Location(5,9);
        bear = new Bear(center);
        world.setTile(outsideOfTerritory, bear);

        int energyBefore = bear.getEnergy();
        bear.act(world);

        assertTrue(bear.getEnergy() < energyBefore-1);
        assertTrue(bear.isInsideTerritory(world.getLocation(bear)));
    }

    /**
     * Test that a bear can eat from a carcass and gain energy.
     */
    @Test
    void bearEatsCarcassAndGainsEnergy() {
        Location carcassLoc = new Location(5, 6);
        Carcass carcass = new Carcass(50, 10);
        world.setTile(carcassLoc, carcass);

        bear.setEnergy(20);
        bear.act(world);

        assertTrue(bear.getEnergy() > 20);
    }

    /**
     * Test that a bear can eat berries from a bush and gain energy.
     */
    @Test
    void bearEatsBushBerries() {
        Location bushLoc = new Location(6, 5);
        Bush bush = new Bush();
        world.setTile(bushLoc, bush);
        for (int i = 0; i < 100; i++) {
            bush.act(world);
        }

        bear.setEnergy(10);
        bear.act(world);

        assertTrue(bear.getEnergy() > 10);
    }

    /**
     * Test that a bear damages a wolf when they fight, decreasing the wolf's energy or killing it.
     */
    @Test
    void bearFightsWolf() {
        Location wolfLoc = new Location(5, 6);
        Wolf wolf = new Wolf(null);
        world.setTile(wolfLoc, wolf);

        int wolfEnergyBefore = wolf.getEnergy();
        bear.setEnergy(40);
        for (int j = 0; j < 3; j++) {
            bear.act(world);
        }

        assertTrue((wolf.getEnergy() < wolfEnergyBefore) || !wolf.isAlive());
    }
}
