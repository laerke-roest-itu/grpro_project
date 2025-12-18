import Inanimate.Grass;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;


import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The GrassTest class verifies the core behavioral logic of the {@link Grass} actor
 * within a simulated {@link World}.
 * The tests ensure that a Grass interacts correctly with its environment,
 * specifically its ability to spread to adjacent tiles based on a defined probability.
 */
class GrassTest {
    World w10;

    /**
     * Sets up the world before each test.
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
     * Test that Grass spreads with correct probability.
     * (should spread to a neighbouring tile approx. 5% of the time over 100 trials)
     */
    @Test
    public void GrassIsSpreadingWithCorrectProbability() {
        int count = 0;

        for (int i = 0; i < 100; i++) {
            Grass g = new Grass();
            Location l = new Location(5,5);
            Location neighbourTile = new Location(5,4);
            Set<Location> neighbours = w10.getSurroundingTiles(l);

            w10.setCurrentLocation(l);
            w10.setTile(l, g);
            g.act(w10);

            if (w10.containsNonBlocking(neighbourTile)) {
                    count++;
            }

            for (Location n : neighbours) {
                if (w10.containsNonBlocking(n)) {
                    Object new_grass = w10.getNonBlocking(n);
                    w10.delete(new_grass);
                }
            }
            w10.delete(g);
        }
        assertTrue( count <= 12);
    }

    /**
     * Test that Grass spreads to all neighbouring tiles when Random is forced to always spread.
     */
    @Test
    public void GrassIsSpreadingToAllNeighbouringTiles() {
        Random alwaysSpread = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };

        Grass g = new Grass(alwaysSpread);
        Location l = new Location(5,5);
        Set<Location> neighbours = w10.getSurroundingTiles(l);

        w10.setCurrentLocation(l);
        w10.setTile(l, g);

        g.spread(w10);

        for (Location n : neighbours) {
            assertTrue(w10.containsNonBlocking(n));
            assertInstanceOf(Grass.class, w10.getNonBlocking(n));
        }
    }

}
