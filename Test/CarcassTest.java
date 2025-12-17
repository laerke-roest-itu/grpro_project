import Actors.Bear;
import Actors.Carcass;
import Inanimate.Fungi;
import Inanimate.Grass;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CarcassTest {

    private Program program;
    private World world;

    private Location loc;

    // Random som vi kan styre
    static class FixedRandom extends Random {
        private final double value;
        FixedRandom(double value) { this.value = value; }
        @Override public double nextDouble() { return value; }
    }

    @BeforeEach
    void setUp() {
        program = new Program(10, 500, 0);
        world = program.getWorld();
        world.setDay(); // tid er ikke super vigtig her, men fint at være eksplicit
        loc = new Location(5, 5);
    }

    @AfterEach
    void tearDown() {
        program = null;
        world = null;
        loc = null;
    }

    @Test
    void eatenReducesMeatLeft_andClampsAtZero() {
        Carcass carcass = new Carcass(50, 10);
        world.setTile(loc, carcass);
        Bear bear = new Bear(new Location(5, 6));
        world.setTile(new Location(5, 6), bear);

        bear.setEnergy(10);
        int before = carcass.getMeatLeft();

        bear.act(world);
        assertTrue(carcass.getMeatLeft() < before);

        carcass.eaten(999);
        assertEquals(0, carcass.getMeatLeft(), "Meat should not go below 0");
    }

    @Test
    void actDeletesCarcassWhenRotTimerRunsOut() {
        Carcass carcass = new Carcass(10, 1); // 1 tick tilbage
        world.setTile(loc, carcass);

        carcass.act(world);

        // Carcass skal være slettet fra verden
        assertFalse(world.contains(carcass), "Carcass should be deleted from world when rotTimer <= 0");
        assertNull(world.getTile(loc), "Tile should be empty after carcass disappears (no fungi)");
    }

    @Test
    void actDeletesCarcassWhenMeatRunsOut() {
        Carcass carcass = new Carcass(1, 10);
        world.setTile(loc, carcass);

        carcass.eaten(1); // meatLeft = 0
        carcass.act(world);

        assertFalse(world.contains(carcass), "Carcass should be deleted when meatLeft <= 0");
    }

    @Test
    void carcassWithFungiSpawnsFungiWhenItDisappears() {
        // rotTimer=1 så den dør med det samme ved act()
        Carcass carcass = new Carcass(10, 1, true);
        world.setTile(loc, carcass);

        carcass.act(world);

        // Carcass er væk
        assertFalse(world.contains(carcass));

        // Der skal nu ligge Fungi på samme tile (som non-blocking)
        Object nb = world.getNonBlocking(loc);
        assertTrue(nb instanceof Fungi, "Fungi should be spawned on the tile when carcass had fungi");
    }

    @Test
    void carcassWithFungiOverwritesGrassWhenItDisappears() {
        // læg græs som non-blocking først
        world.setTile(loc, new Grass());

        // find en nabo til carcass (blocking), fordi grass allerede bruger non-blocking layer på loc
        Location carcassLoc = new Location(5, 6);
        Carcass carcass = new Carcass(10, 1, true);
        world.setTile(carcassLoc, carcass);

        // flyt grass over på carcassLoc så det matcher din "overskriv grass på samme felt"-logik
        // (vi skal have grass + carcass på samme koordinat => grass non-blocking, carcass blocking)
        world.delete(world.getNonBlocking(loc));
        world.setTile(carcassLoc, new Grass());

        carcass.act(world);

        Object nb = world.getNonBlocking(carcassLoc);
        assertTrue(nb instanceof Fungi, "Fungi should overwrite Grass when carcass disappears");
    }

    @Test
    void carcassCanBecomeInfectedViaRandom() {
        // random=0.0 => altid < 0.05 => trySpawnFungi() sætter hasFungi=true
        Carcass carcass = new Carcass(10, 2, false, new FixedRandom(0.0));
        world.setTile(loc, carcass);

        // første act: rotTimer går 2->1, og hasFungi bliver true
        carcass.act(world);

        // anden act: hvis hasFungi==true, rotTimer-- ekstra, så den dør hurtigere
        carcass.act(world);

        // Den bør være væk nu (meget sandsynligt med rotTimer=2 og ekstra decrement)
        assertFalse(world.contains(carcass), "Carcass should rot faster after being infected");
    }
}

