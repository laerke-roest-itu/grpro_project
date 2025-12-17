import Inanimate.Fungi;
import Actors.Carcass;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class FungiTest {

    private Program program;
    private World world;

    private Fungi fungi;
    private Location fungiLoc;

    @BeforeEach
    void setUp() {
        program = new Program(12, 500, 0);
        world = program.getWorld();

        world.setDay();
        world.setCurrentTime(0);

        fungiLoc = new Location(5, 5);
        fungi = new Fungi(3); // lille lifespan så vi kan teste death hurtigt
        world.setTile(fungiLoc, fungi);
    }

    @AfterEach
    void tearDown() {
        program = null;
        world = null;
        fungi = null;
    }

    @Test
    void fungiLifespanDecreasesEachAct() {
        int before = fungi.getLifespan();
        fungi.act(world);
        assertEquals(before - 1, fungi.getLifespan());
    }

    @Test
    void fungiDeletesItselfWhenLifespanHitsZero() {
        // lifespan = 3 -> efter 3 acts bør den være slettet
        fungi.act(world); // 2
        fungi.act(world); // 1
        fungi.act(world); // 0 => delete

        assertFalse(world.contains(fungi), "Fungi should be deleted from world when lifespan <= 0");
    }

    @Test
    void fungiInfectsCarcassWithinRadiusTwo() {
        // Carcass indenfor radius 2 fra (5,5): fx (7,5) har distance 2
        Location carcassLoc = new Location(7, 5);
        Carcass carcass = new Carcass(50, 10); // starter uden fungi
        world.setTile(carcassLoc, carcass);

        // Før: rotTimer falder normalt 1 pr tick.
        // Efter infektion: rotTimer falder 2 pr tick (pga. if(hasFungi) rotTimer--; + rotTimer--;)
        // Vi kan ikke læse rotTimer direkte, så vi må teste via "hvor hurtigt den forsvinder".

        // Hvis den IKKE blev inficeret, burde den leve ~10 acts før delete.
        // Hvis den BLEV inficeret hurtigt, burde den dø tidligere (~5 acts).
        // Vi kører: fungi.act én gang (infektion), og derefter carcass.act gentagne gange.
        fungi.act(world); // infektion sker her

        // kør carcass frem til den forsvinder, men med en max-sikring
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
