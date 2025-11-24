import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

    public class GrassTest {
        World w10;

        @BeforeEach
        public void setUp() {
            w10 = new World(10);
        }

        @AfterEach
        public void tearDown() {
            w10 = null;
        }

        @Test
        public void GrassIsSpreadingWithCorrectProbability() {
            int count = 0;
            for (int i = 0; i < 100; i++) {
                Grass g = new Grass();
                Location l = new Location(5, 5);
                w10.setCurrentLocation(l);
                w10.setTile(l, g);
                g.act(w10);
                if () {
                    count++;

                }

                w10.delete(g);
            }
            Assertions.assertTrue(count >= 0 && count <= 10);
        }

        @Test
        public void GrassIsSpreadingToAllNeighbouringTiles() {

        }
    }
