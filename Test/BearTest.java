import Actors.Bear;
import Actors.Wolf;
import Inanimate.Bush;
import Actors.Carcass;
import itumulator.world.Location;
import itumulator.world.World;
import itumulator.executable.Program;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class BearTest {

    World world;
    Bear bear;
    Location center;

    @BeforeEach
    void setUp() {
        Program program = new Program(10, 500, 0);
        world = program.getWorld();

        center = new Location(5, 5);
        bear = new Bear(center);
        world.setTile(center, bear);
    }

    @AfterEach
    void tearDown() {
        world = null;
        bear = null;
    }

    @Test
    void bearLosesEnergyWhenActing_and_movesTowardsTerritoryCenter() {
        world.delete(bear);

        Location outsideOfTerritory = new Location(5,9);
        bear = new Bear(center);
        world.setTile(outsideOfTerritory, bear);

        int energyBefore = bear.getEnergy();
        bear.act(world);

        assertTrue(bear.getEnergy() < energyBefore-1);
        assertTrue(bear.isInsideTerritory(world.getLocation(bear)));
    }

    @Test
    void bearEatsCarcassAndGainsEnergy() {
        Location carcassLoc = new Location(5, 6);
        Carcass carcass = new Carcass(50, 10);
        world.setTile(carcassLoc, carcass);

        bear.setEnergy(20);
        bear.act(world);

        assertTrue(bear.getEnergy() > 20);
    }

    @Test
    void bearEatsBushBerries() {
        Location bushLoc = new Location(6, 5);
        Bush bush = new Bush();
        world.setTile(bushLoc, bush);
        for (int i = 0; i < 100; i++) {
            bush.act(world);
        }

        bear.setEnergy(10);
        bear.act(world);

        assertTrue(bear.getEnergy() > 10);
    }

    @Test
    void bearFightsWolf() {
        Location wolfLoc = new Location(5, 6);
        Wolf wolf = new Wolf(null);
        world.setTile(wolfLoc, wolf);

        int wolfEnergyBefore = wolf.getEnergy();
        bear.setEnergy(40);
        for (int j = 0; j < 3; j++) {
            bear.act(world);
        }

        assertTrue((wolf.getEnergy() < wolfEnergyBefore) || !wolf.isAlive());
    }
}
