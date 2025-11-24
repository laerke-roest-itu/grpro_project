import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;


import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GrassTest {
    World w10;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        w10 = new World(10);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        w10 = null;
    }

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

    @Test
    public void GrassIsSpreadingToAllNeighbouringTiles() {
        Random alwaysSpread = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0; // tvinger spredning
            }
        };

        Grass g = new Grass(alwaysSpread);
        Location l = new Location(5,5);
        Set<Location> neighbours = w10.getSurroundingTiles(l);

        w10.setCurrentLocation(l);
        w10.setTile(l, g);

        g.act(w10);

        // assert: der er græs på ALLE nabofelter
        for (Location n : neighbours) {
            assertTrue(w10.containsNonBlocking(n));
            assertInstanceOf(Grass.class, w10.getNonBlocking(n));
        }
    }

}
