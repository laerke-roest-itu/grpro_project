import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import itumulator.world.World;
import itumulator.world.Location;

import java.lang.reflect.Field;
import java.util.Set;

public class BearTest {
    private World w10;
    private Location territoryCenter;
    private Bear bear;

    @BeforeEach
    void setUp() {
        w10 = new World(10);
        territoryCenter = new Location(5, 5);
        bear = new Bear(territoryCenter);
    }

    @AfterEach
    void tearDown() {
        w10 = null;
        bear = null;
        territoryCenter = null;
    }

    // ========== HJÆLPEMETODER ==========

    /**
     * Placer et "blocking" objekt i verden.
     * Ret denne metode, hvis jeres World har en anden API
     * (fx w10.place(obj, loc) i stedet for setTile(...)).
     */
    private void placeBlocking(Object obj, Location loc) {
        w10.setTile(loc, obj);   // RET HER hvis det hedder noget andet hos jer
    }


    /** Manhattan-distance helper (samme idé som typisk distance-funktion i Animal). */
    private int manhattan(Location a, Location b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    // ========== TESTS FOR act() ==========

    @Test
    void act_whenOutsideTerritory_movesTowardsTerritoryCenter() {
        // Arrange: placer bjørnen langt væk fra center
        Location start = new Location(0, 0);
        placeBlocking(bear, start);

        int distBefore = manhattan(start, territoryCenter);

        // Act
        bear.act(w10);

        // Assert: ny position er en nabo til start og tættere på center
        Location after = w10.getLocation(bear);
        assertNotNull(after, "Bjørnen skal have en position i verden.");
        assertNotEquals(start, after, "Bjørnen skal have bevæget sig.");

        // after skal være en af de omkringliggende felter
        Set<Location> neighbors = w10.getSurroundingTiles(start);
        assertTrue(neighbors.contains(after),
                "Bjørnen skal kun bevæge sig ét skridt ad gangen.");

        int distAfter = manhattan(after, territoryCenter);
        assertTrue(distAfter < distBefore,
                "Bjørnen skal være tættere på territoriecenteret efter act().");
    }

    @Test
    void act_whenPreyInTerritory_movesOneStepTowardsPrey() {
        // Arrange: bjørn i territoriecenter
        Location bearStart = territoryCenter;
        placeBlocking(bear, bearStart);

        // Bytte (Rabbit) inde i territoriet men ikke direkte nabo
        Location preyLoc = new Location(7, 5); // afstand 2 horisontalt
        Rabbit rabbit = new Rabbit();          // antager default ctor findes
        placeBlocking(rabbit, preyLoc);

        int distBefore = manhattan(bearStart, preyLoc);

        // Act
        bear.act(w10);

        // Assert
        Location bearAfter = w10.getLocation(bear);
        assertNotNull(bearAfter);
        assertNotEquals(bearStart, bearAfter,
                "Bjørnen skal bevæge sig mod byttet.");

        // Ét skridt ad gangen
        Set<Location> neighbors = w10.getSurroundingTiles(bearStart);
        assertTrue(neighbors.contains(bearAfter),
                "Bjørnen skal kun tage ét skridt mod byttet.");

        int distAfter = manhattan(bearAfter, preyLoc);
        assertTrue(distAfter < distBefore,
                "Bjørnen skal være tættere på byttet efter act().");
    }

    @Test
    void act_whenAdjacentPreyInTerritory_eatsPrey() {
        // Arrange: bjørn i center
        Location bearLoc = territoryCenter;
        placeBlocking(bear, bearLoc);

        // Bytte lige ved siden af bjørnen
        Location preyLoc = new Location(6, 5); // nabo til (5,5)
        Rabbit rabbit = new Rabbit();
        w10.setTile(preyLoc, rabbit);

        // Husk energi før (for at tjekke +40)
        int energyBefore = bear.getEnergy();

        // Act
        bear.act(w10); //skal trække én fra, når vi skal assertEquals, da act() koster energi

        // Assert:
        // 1) Byttet skal være fjernet fra verden
        Object tileContent = w10.getTile(preyLoc);
        assertNull(tileContent,
                "Når bjørnen står ved siden af byttet, skal den spise det (tile skal være tom).");

        // 2) Energi skal være steget med 40 (jf. getFoodEnergy)
        int energyAfter = bear.getEnergy();
        assertEquals(energyBefore + 40 - 1, energyAfter,
                "Bjørnen skal få +40 energi efter at have spist et byttedyr.");
    }

    @Test
    void act_whenHungryAndBushNearby_eatsBerries() {
        // Arrange: bjørn i territoriet
        Location bearLoc = territoryCenter;
        w10.setTile(bearLoc, bear);

        // Gør bjørnen sulten: energy < 50
        bear.setEnergy(10);
        int energyBefore = bear.getEnergy();

        // Placer en Bush på nabo-felt
        Location bushLoc = new Location(6, 5); // nabo
        Bush bush = new Bush();                // antager default ctor
        w10.setTile(bushLoc, bush);

        for (int i = 0; i < 100; i++) {
            bush.act(w10);
        }

        int berriesBefore = bush.getBerryCount();

        // Act

        bear.act(w10); //skal lægge én til, når vi skal assertEquals, da act() koster energi

        // Assert:
        // 1) Bjørnens energi skal være steget med berryCount
        int energyAfter = bear.getEnergy();
        assertEquals(energyBefore + berriesBefore, energyAfter + 1,
                "Bjørnen skal få energi svarende til antal bær i busken.");

        // 2) Busken skal have fået reduceret sine bær
        int berriesAfter = bush.getBerryCount();
        assertTrue(berriesAfter < berriesBefore,
                "Buskens antal bær skal være reduceret efter bjørnen har spist.");
    }
}
