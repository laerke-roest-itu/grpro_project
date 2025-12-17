import Actors.Bear;
import Inanimate.Bush;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field; //used to find the protected 'energy' field (Field) from the superclass Animal
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BushTest {
    World w10;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        w10 = new World(10);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        w10 = null;
    }

    /** Test that Bush spreads with correct probability - as the spreading probability per tick is 3% for bush
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
        assertEquals(3, b.getBerryCount(), "Bush should have produced exactly 3 berries.");
    }

    /**
     * Verifies that a Bear can eat berries from a neighbouring Bush.
     *
     * Reflection (java.lang.reflect.Field) is used to modify the protected energy field in the Animal superclass
     * in order to force the Bear into a hungry state and make the test deterministic.
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
        assertEquals(4, b.getBerryCount());


        Bear bear = new Bear(bearLoc);

        // Force bear to be hungry by appointing a value to the "protected" Energy field (from Animal superclass)
        Field energyField = null;
        Class<?> cls = bear.getClass();

        while (cls != null) {
            try {
                energyField = cls.getDeclaredField("energy"); //'Field' that is tagged onto the end of 'energy' uses
                break;                                              //import java.lang.reflect.Field; to look for the field
            } catch (NoSuchFieldException e) {                      // 'energy' and use the field at energyField
                cls = cls.getSuperclass();
            }
        }

        assertNotNull(energyField, "Could not find 'energy' in Bear/Animal - adjust test.");
        energyField.setAccessible(true);
        energyField.setInt(bear, 10); // low energy, so Bear will try to eat berries from Bush.


        w10.setTile(bearLoc, bear);

        // let Bear act() to detect Bush and eat berries from Bush
        bear.act(w10);

        // Bear eat()-method eats all berries on bush.
        assertFalse(b.hasBerries(), "Bush should have 0 berries after Bear has eaten them.");
        assertEquals(0, b.getBerryCount());


        int finalEnergy = energyField.getInt(bear);
        assertEquals(17, finalEnergy,
                "Bear energy should have increased with the amount of berries eaten(4) to a total of 17 energy.");

    }

}

