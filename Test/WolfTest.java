import Actors.*;
import Inanimate.*;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class WolfTest {

    private World world;
    private Pack pack;
    private Wolf leader;
    private Wolf wolf;

    @BeforeEach
    void setUp() {
        Program program = new Program(10, 500, 0);
        world = program.getWorld();

        pack = new Pack(); // din pack-klasse der også kan claimDen

        leader = new Wolf(pack);
        wolf   = new Wolf(pack);

        Location leaderLoc = new Location(2, 2);
        world.setTile(leaderLoc, leader); // leader
        world.setTile(new Location(2, 4), wolf);   // medlem
    }

    @AfterEach
    void tearDown() {
        world = null;
        pack = null;
        leader = null;
        wolf = null;
    }

    @Test
    void wolfSeeksLeaderWhenInPack_whenHungryAndNoTargets() {
        // gør ulven sulten så den IKKE går random (hunt bliver kaldt, men finder intet)
        wolf.setEnergy(10);

        Location before = world.getLocation(wolf);
        Location leaderLoc = world.getLocation(leader);

        wolf.act(world);

        Location after = world.getLocation(wolf);

        int distBefore = wolf.distance(before, leaderLoc);
        int distAfter  = wolf.distance(after, leaderLoc);

        assertTrue(distAfter < distBefore, "Wolf should move closer to leader via seekPack()");
    }

    @Test
    void wolfEatsCarcassAndReducesMeatLeft() {
        // Lav en ulv uden pack, så den ikke bruger seekPack før jagt
        Wolf solo = new Wolf(null);
        Location wolfLoc = new Location(5, 5);
        world.setTile(wolfLoc, solo);

        solo.setEnergy(10); // sulten

        Location carcassLoc = new Location(5, 6); // nabo med det samme
        Carcass carcass = new Carcass(50, 10);
        world.setTile(carcassLoc, carcass);

        int meatBefore = carcass.getMeatLeft();
        solo.act(world);

        assertTrue(carcass.getMeatLeft() < meatBefore,
                "Wolf should eat carcass and reduce meat left");
    }


    @Test
    void wolfKillsRabbitWhenHunting() {
        wolf.setEnergy(10); // sulten

        // Samme trick: rabbit ved siden af (2,3) efter seekPack
        Location rabbitLoc = new Location(3, 3);
        world.setTile(rabbitLoc, new Rabbit());

        // giv den et par turns (først gå hen, så dræbe)
        for (int i = 0; i < 5; i++) wolf.act(world);

        assertTrue(world.getTile(rabbitLoc) instanceof Carcass,
                "Rabbit should be replaced by Carcass after being killed");
    }

    @Test
    void wolfFightsBearWhenEnemyNearby() {
        wolf.setEnergy(40);

        Location bearLoc = new Location(3, 3);
        Bear bear = new Bear(new Location(5, 5));
        world.setTile(bearLoc, bear);

        int bearBefore = bear.getEnergy();
        int wolfBefore = wolf.getEnergy();

        for (int i = 0; i < 5; i++) wolf.act(world);

        boolean bearDamagedOrDead =
                bear.getEnergy() < bearBefore || world.getTile(bearLoc) instanceof Carcass;

        boolean wolfDamagedOrDead =
                wolf.getEnergy() < wolfBefore || world.getLocation(wolf) == null;

        assertTrue(bearDamagedOrDead);
        assertTrue(wolfDamagedOrDead);
    }

    @Test
    void leaderBuildsDenAndPackClaimsIt() {
        assertNull(pack.getDen(), "Pack should start without a den");

        Location leaderLoc = world.getLocation(leader);
        assertNotNull(leaderLoc);

        leader.buildDen(world);

        Den den = pack.getDen();
        assertNotNull(den, "Pack should have claimed a den");

        // Pack.claimDen skal give alle ulve samme den-reference
        assertSame(den, leader.getDen());
        assertSame(den, wolf.getDen());

        // Dens lokation (den er placeret i world med setTile)
        Location denLoc = world.getLocation(den);
        assertNotNull(denLoc, "Den should exist in the world");

        // Den skal ligge på et nabofelt til leader (radius 1)
        assertTrue(world.getSurroundingTiles(leaderLoc, 1).contains(denLoc),
                "Den should be placed next to the leader");

        // Den ligger som blocking tile på denLoc
        assertTrue(world.getTile(denLoc) instanceof Den,
                "Den should be placed as a tile at its location");
    }


}
