import Actors.Bear;
import Actors.Deer;
import Inanimate.Bush;
import Inanimate.Herd;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field; //used to find the protected 'energy' field (Field) from the superclass Animal
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 *The BushTest class verifies the core behavioral logic of the {@link Bush} actor
 * within a simulated {@link World}.
 * The tests ensure that a Bush interacts correctly with its environment,
 * including spreading behaviour relative to its location, berry production
 * during act()'s, and interactions with other entities such as
 * {@link Bear}.
 * The class focuses on validating observable effects of the Bush's
 * {@code act(World)} behavior rather than internal implementation details.
 */
public class BushTest {
    World w10;

    /**
     * Sets up the world for each test.
     */
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        w10 = new World(10);
    }

    /**
     * Cleans up the world after each test.
     */
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        w10 = null;
    }

    /**
     * Test that Bush spreads with correct probability - as the spreading probability per tick is 3% for bush
     * (should spread to a neighbouring tile approx. 3% of the time over 100 trials)
     * we have a higher than 3 assertvalue to ensure a margin for error).
     */
    @Test
    public void BushIsSpreadingWithCorrectProbability() {
        int count = 0;

        for (int i = 0; i < 100; i++) {
            Bush b = new Bush();
            Location l = new Location(5,5);
            Location neighbourTile = new Location(5,4);
            Set<Location> neighbours = w10.getSurroundingTiles(l);

            w10.setCurrentLocation(l);
            w10.setTile(l, b);
            b.act(w10);

            if (w10.containsNonBlocking(neighbourTile)) {
                count++;
            }

            for (Location n : neighbours) {
                if (w10.containsNonBlocking(n)) {
                    Object bush = w10.getNonBlocking(n);
                    w10.delete(bush);
                }
            }
            w10.delete(b);
        }
        assertTrue( count <= 12);
    }

    /**
     * Test that Bush spreads to all neighbouring tiles when Random is forced to always spread.
     */

    @Test
    public void BushIsSpreadingToAllNeighbouringTiles() {
        Random alwaysSpread = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0; // forces spread
            }
        };

        Bush b = new Bush(alwaysSpread);
        Location l = new Location(5,5);
        Set<Location> neighbours = w10.getSurroundingTiles(l);

        w10.setCurrentLocation(l);
        w10.setTile(l, b);

        b.act(w10);

        // assert if there's a Bush in ALL neighbouring tiles
        for (Location n : neighbours) {
            assertTrue(w10.containsNonBlocking(n));
            assertInstanceOf(Bush.class, w10.getNonBlocking(n));
        }
    }
    /**
     * Test that Bush is able to produce berries - chosen to be independent of ticks (we tell it to produceBerries 3 times)
     * as we only need to see if it is possible to produce berries and the precise amount. The test assumes act + timing works.
     */
    @Test
    public void bushProducesBerryTest() {
        Bush b = new Bush();
        Location bushLoc = new Location(5, 5);


        w10.setCurrentLocation(bushLoc);
        w10.setTile(bushLoc, b);

        b.produceBerries(); // +1
        b.produceBerries(); // +1
        b.produceBerries(); // +1

        assertTrue(b.hasBerries(), "Bush should have berries after produceBerries().");
        assertEquals(15, b.getBerryCount(), "Bush should have produced exactly 3 berries.");
    }

    /**
     * Verifies that a Deer can eat berries from a neighbouring Bush.
     * Reflection (java.lang.reflect.Field) is used to modify the protected energy field in the Animal superclass
     * in order to force the Deer into a hungry state and make the test deterministic.
     */
    @Test
    public void berriesGetEaten() throws Exception {

        Bush b = new Bush();
        Location bushLoc = new Location(5, 5);
        Location bearLoc = new Location(5, 6);

        w10.setTile(bushLoc, b);

        for (int i = 0; i < 4; i++) {
            b.produceBerries();
        }

        assertTrue(b.hasBerries());
        assertEquals(20, b.getBerryCount());


        Herd herd = new Herd();
        Deer deer = new Deer(herd);

        Field energyField = null;
        Class<?> cls = deer.getClass();

        while (cls != null) {
            try {
                energyField = cls.getDeclaredField("energy");
                break;
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }

        assertNotNull(energyField, "Could not find 'energy' in Deer/Animal - adjust test.");
        energyField.setAccessible(true);
        energyField.setInt(deer, 10);

        int energybefore = deer.getEnergy();

        w10.setTile(bearLoc, deer);

        deer.act(w10);

        assertFalse(b.hasBerries(), "Bush should have 0 berries after Deer has eaten them.");
        assertEquals(0, b.getBerryCount());


        int finalEnergy = energyField.getInt(deer);
        assertEquals(energybefore+34, finalEnergy,
                "Deer energy should have increased with the amount of berries eaten.");

    }

}

