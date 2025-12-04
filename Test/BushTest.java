import Inanimate.Bush;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field; //anvendt for at finde Energy feltet (Field) fra superklassen animal
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
            w10.delete(g);
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
    @Test
    public void bushProducesBerryTest() {
        Bush b = new Bush();                 // anvender default Random
        Location bushLoc = new Location(5, 5);

        // placer en Bush i verdenen
        w10.setCurrentLocation(bushLoc);
        w10.setTile(bushLoc, b);

        // producer Berry nogle gange direkte
        // (hurtigere end at kalde act – testen antager at act + timing virker)
        b.produceBerries(); // +1
        b.produceBerries(); // +1
        b.produceBerries(); // +1

        assertTrue(b.hasBerries(), "Bush bør have berries efter produceBerries()");
        assertEquals(3, b.getBerryCount(), "Bush bør have produceret præcis 3 berries.");
    }
// testen ser bare om det er muligt at producere berries i stedet for at vente flere ticks


    @Test
    public void berriesGetEaten() throws Exception {
        // laver en Bush med berries og en Bear der er hungry ved siden af den
        Bush b = new Bush();
        Location bushLoc = new Location(5, 5);
        Location bearLoc = new Location(5, 6); // problemer med bear ender med et output på 8 istedet for forventet 14

        w10.setTile(bushLoc, b);

        // Laver flere berries på bush
        for (int i = 0; i < 4; i++) {
            b.produceBerries();
        }

        assertTrue(b.hasBerries());
        assertEquals(4, b.getBerryCount());

        // Laver en Bear hvis territoriecenter er dens egen lokation
        Bear bear = new Bear(bearLoc);

        // Tving Bear til at være hungry ved at sætte "protected" feltet energy (fra Animal superklassen)
        Field energyField = null;
        Class<?> cls = bear.getClass();

        while (cls != null) {
            try {
                energyField = cls.getDeclaredField("energy"); //Field der står i enden efter energy anvender
                break;                                              //import java.lang.reflect.Field; til at lede efter
            } catch (NoSuchFieldException e) {                      // "energy" og derefter bruge feltet ved energyField
                cls = cls.getSuperclass();
            }
        }

        assertNotNull(energyField, "Kunne ikke finde 'energy' i Bear/Animal - juster test.");
        energyField.setAccessible(true);
        energyField.setInt(bear, 10); // lav energi, så bear vil prøve at spise berries

        // placer Bear tæt på Bush
        w10.setTile(bearLoc, bear);

        // Act: lad bear act() én gang — den burde finde bush i neighbourTile,
        // spise berries og kalde berriesEaten()
        bear.act(w10);

        // bush bør nu have 0 berries
        assertFalse(b.hasBerries(), "Bush bør have 0 berries efter Bear har spist dem.");
        assertEquals(0, b.getBerryCount());

        // Bear energy bør være steget med antallet af berries den spiste (4)
        int finalEnergy = energyField.getInt(bear);
        assertEquals(13, finalEnergy,
                "Bear energy bør være steget med antallet af berries den har spist (4).");
        // bør blive 10-1 (pga. act der tager -1 energy) og så +4 fra berries så 13
        // laver 02.12.2025 stadigt fejl, kan være det har noget med Energy systemet i Animal-klassen at gøre
    }

}

