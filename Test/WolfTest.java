import Actors.*;
import Inanimate.*;
import itumulator.executable.Program;
import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WolfTest class verifies the core behavioral logic of the {@link Wolf} actor
 * within a simulated {@link World}.
 *
 * The tests ensure that a Wolf interacts correctly with its environment,
 * including seeks leader when in a pack, eats Carcass and reduces the meat left,
 * kills a rabbit while hunting, fights a Bear when registered as a nearby enemy,
 * doesn't deadlock a leader when the Wolf actors in a pack are reintroduced
 * into the world after a night cycle and a leader of a pack digs a Den and claims it.
 *
 * The class focuses on validating observable effects of the Wolf's
 * {@code act(World)} behavior rather than internal implementation details.
 */

class WolfTest {

    private World world;
    private Pack pack;
    private Wolf leader;
    private Wolf wolf;

    @BeforeEach
    void setUp() {
        Program program = new Program(10, 500, 0);
        world = program.getWorld();

        pack = new Pack();

        leader = new Wolf(pack);
        wolf   = new Wolf(pack);

        Location leaderLoc = new Location(2, 2);
        world.setTile(leaderLoc, leader);
        world.setTile(new Location(2, 4), wolf);
    }

    @AfterEach
    void tearDown() {
        world = null;
        pack = null;
        leader = null;
        wolf = null;
    }

    /**
     * Test that a wolf moves closer to its pack leader when they are far apart.
     */
    @Test
    void wolfSeeksLeaderWhenInPack() {
        Location before = world.getLocation(wolf);
        Location leaderLoc = world.getLocation(leader);

        wolf.act(world);

        Location after = world.getLocation(wolf);

        int distBefore = wolf.distance(before, leaderLoc);
        int distAfter  = wolf.distance(after, leaderLoc);

        assertTrue(distAfter < distBefore, "Wolf should move closer to leader via seekPack()");
    }

    /**
     * Test that a wolf eats from a carcass and reduces its meat level.
     */
    @Test
    void wolfEatsCarcassAndReducesMeatLeft() {
        Wolf solo = new Wolf(null);
        Location wolfLoc = new Location(5, 5);
        world.setTile(wolfLoc, solo);

        solo.setEnergy(10);

        Location carcassLoc = new Location(5, 6);
        Carcass carcass = new Carcass(50, 10);
        world.setTile(carcassLoc, carcass);

        int meatBefore = carcass.getMeatLeft();
        solo.act(world);

        assertTrue(carcass.getMeatLeft() < meatBefore,
                "Wolf should eat carcass and reduce meat left");
    }


    /**
     * Test that a wolf successfully hunts and kills a rabbit, leaving a carcass.
     */
    @Test
    void wolfKillsRabbitWhenHunting() {
        wolf.setEnergy(10);

        Location rabbitLoc = new Location(3, 3);
        world.setTile(rabbitLoc, new Rabbit());

        for (int i = 0; i < 5; i++) wolf.act(world);

        assertTrue(world.getTile(rabbitLoc) instanceof Carcass,
                "Rabbit should be replaced by Carcass after being killed");
    }

    /**
     * Test that a wolf fights back when a bear is nearby.
     */
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


    /**
     * Test that a wolf does not move when its pack leader is surrounded by other pack members,
     * ensuring no deadlock occurs and the leader can eventually move.
     */
    @Test
    void packDoesNotDeadlockLeader() {
        // Clear world from setUp
        for (Object entity : new java.util.HashSet<>(world.getEntities().keySet())) {
            if (entity instanceof Actor) {
                Location loc = world.getLocation(entity);
                if (loc != null) world.delete(entity);
            }
        }

        Pack deadlockPack = new Pack();
        Wolf leaderWolf = new Wolf(deadlockPack);
        Location leaderLoc = new Location(5, 5);
        world.setTile(leaderLoc, leaderWolf);

        // Surround leader with wolves
        Set<Location> surrounding = world.getSurroundingTiles(leaderLoc);
        for (Location loc : surrounding) {
            Wolf member = new Wolf(deadlockPack);
            world.setTile(loc, member);
        }

        // Initially, the leader cannot move because it's surrounded
        assertTrue(world.getEmptySurroundingTiles(leaderLoc).isEmpty(), "Leader should be surrounded");

        // Act with all members first, they should move away or perform actions
        for (Object entity : new java.util.ArrayList<>(world.getEntities().keySet())) {
            if (entity instanceof Wolf && entity != leaderWolf) {
                ((Wolf) entity).act(world);
            }
        }

        // Now check if there's any empty space for the leader
        Set<Location> emptyAfter = world.getEmptySurroundingTiles(leaderLoc);
        assertFalse(emptyAfter.isEmpty(),
                "Leader should have empty space after members act if they don't deadlock. Empty tiles: " + emptyAfter);

        // Or act with everyone for a few turns and ensure leader moves from (5,5) eventually
        for (int i = 0; i < 5; i++) {
            for (Object entity : new java.util.ArrayList<>(world.getEntities().keySet())) {
                if (entity instanceof Actor) {
                    ((Actor) entity).act(world);
                }
            }
        }

        assertNotEquals(leaderLoc, world.getLocation(leaderWolf), "Leader should have moved from its original position");
    }

    /**
     * Test that the leader of a pack can successfully build a den and that all members
     * of the pack correctly claim it as their home.
     */
    @Test
    void leaderBuildsDenAndPackClaimsIt() {
        assertNull(pack.getDen(), "Pack should start without a den");

        Location leaderLoc = world.getLocation(leader);
        assertNotNull(leaderLoc);

        leader.buildDen(world);

        Den den = pack.getDen();
        assertNotNull(den, "Pack should have claimed a den");

        assertSame(den, leader.getDen());
        assertSame(den, wolf.getDen());

        Location denLoc = world.getLocation(den);
        assertNotNull(denLoc, "Den should exist in the world");

        assertTrue(world.getSurroundingTiles(leaderLoc, 1).contains(denLoc),
                "Den should be placed next to the leader");

        assertTrue(world.getTile(denLoc) instanceof Den,
                "Den should be placed as a tile at its location");
    }


}
