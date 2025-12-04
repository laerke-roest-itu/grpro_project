import Inanimate.Bush;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void BushIsSpreadingToAllNeighbouringTiles() {
        Random alwaysSpread = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0; // tvinger spredning
            }
        };

        Bush b = new Bush(alwaysSpread);
        Location l = new Location(5,5);
        Set<Location> neighbours = w10.getSurroundingTiles(l);

        w10.setCurrentLocation(l);
        w10.setTile(l, b);

        b.act(w10);

        // assert: der er bush på ALLE nabofelter
        for (Location n : neighbours) {
            assertTrue(w10.containsNonBlocking(n));
            assertInstanceOf(Bush.class, w10.getNonBlocking(n));
        }
    }

    // skal lave tests for Inanimate.Bush med
    // BushProducesBerryTest


    // BearEatsBerryTest - vi ligger den i bush klassen fordi den
}

