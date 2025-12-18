import Actors.Bear;
import Actors.Deer;
import Inanimate.Grass;
import Inanimate.Herd;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The DeerTest class verifies the core behavioral logic of the {@link Deer} actor
 * within a simulated {@link World}.
 *
 * The tests ensure that a Deer interacts correctly with its environment,
 * including herd behavior, fleeing from predators, energy consumption while
 * eating {@link Grass}, and reproduction within a {@link Herd}.
 *
 * The class focuses on validating observable effects of the Deer's
 * {@code act(World)} behavior rather than internal implementation details.
 */
class DeerTest {

    private Program program;
    private World world;

    private Herd herd;
    private Deer leader;
    private Deer member;

    private Location leaderLoc;
    private Location memberLoc;

    /**
     * The {@code @BeforeEach} and {@code @AfterEach} methods set up and tear down
     * the world state used by each Deer test as part of the JUnit test lifecycle.
     */
    @BeforeEach
    void setUp() {
        program = new Program(12, 500, 0);
        world = program.getWorld();

        herd = new Herd();

        leader = new Deer(herd);
        member = new Deer(herd);

        leaderLoc = new Location(2, 2);
        memberLoc = new Location(2, 8); // langt væk (distance 6)

        world.setTile(leaderLoc, leader);
        world.setTile(memberLoc, member);

        // start at Day
        world.setDay();
    }

    @AfterEach
    void tearDown() {
        program = null;
        world = null;
        herd = null;
        leader = null;
        member = null;
    }

    /**
     * Test that the herd leader sets the herd's home location during its first action.
     */
    @Test
    void leaderSetsHerdHomeOnFirstAct() {
        assertNull(herd.getHome(), "Home should start as null");

        leader.act(world);

        assertEquals(leaderLoc, herd.getHome(), "Leader should set herd home to its starting position");
    }

    /**
     * Test that a herd member moves closer to the leader when they are too far apart.
     */
    @Test
    void memberMovesCloserToLeaderWhenTooFarAway() {
        // sørg for at leader.home er sat
        leader.act(world);

        Location before = world.getLocation(member);
        Location leaderNow = world.getLocation(leader);

        int distBefore = member.distance(before, leaderNow);

        member.act(world); // i dayBehaviour: dist > 3 => moveOneStepTowards leader

        Location after = world.getLocation(member);
        int distAfter = member.distance(after, leaderNow);

        assertTrue(distAfter < distBefore, "Member deer should move closer to leader when distance > 3");
    }

    /**
     * Test that a deer flees when a predator is nearby.
     */
    @Test
    void deerFleesWhenPredatorIsNearby() {
        // predator close to member (radius 2)
        Location predatorLoc = new Location(3, 7); // close to (2,8)
        Bear predator = new Bear(new Location(10, 10));
        world.setTile(predatorLoc, predator);

        int energyBefore = member.getEnergy();
        Location before = world.getLocation(member);

        member.act(world); // should flee + energy -= 5

        Location after = world.getLocation(member);

        assertNotEquals(before, after, "Deer should move when fleeing");
        assertTrue(member.getEnergy() < energyBefore, "Deer should lose energy when fleeing");

        // should move further away from Predator (typisk)
        int dBefore = member.distance(before, predatorLoc);
        int dAfter  = member.distance(after, predatorLoc);
        assertTrue(dAfter >= dBefore, "Deer should not move closer to predator when fleeing");
    }

    /**
     * Test that a deer seeks shelter at dusk by moving toward its herd's home location.
     */
    @Test
    void deerSeeksShelterAtDusk() {
        // make sure herd home is set

        leader.act(world);

        world.setCurrentTime(World.getTotalDayDuration() - 3);

        Location before = world.getLocation(member);
        member.act(world); // Herbivore.act -> seekShelter() -> moveOneStepTowards(home)

        Location after = world.getLocation(member);
        assertNotEquals(before, after, "Deer should move toward home at dusk");
    }

    /**
     * Test that a deer eats grass and gains energy.
     */
    @Test
    void deerEatsGrassAndGainsEnergy() {
        world.setDay();

        Deer solo = new Deer(null);
        Location start = new Location(6, 6);
        world.setTile(start, solo);

        solo.setEnergy(10); // Deer is forced to be hungry

        // plant grass on surrounding tiles
        for (Location n : world.getSurroundingTiles(start, 1)) {
            if (world.isTileEmpty(n) && !world.containsNonBlocking(n)) {
                world.setTile(n, new Grass());
            }
        }

        int before = solo.getEnergy();
        solo.act(world);

        assertTrue(solo.getEnergy() > before, "Deer should gain energy from eating grass");
    }

    /**
     * Test that a deer can find and eat grass within a distance of 2.
     */
    @Test
    void deerEatsGrassAtDistance2() {
        world.setDay();
        Deer solo = new Deer(null);
        Location start = new Location(6, 6);
        world.setTile(start, solo);
        solo.setEnergy(40); // Hungry

        Location foodLoc = new Location(6, 8); // Distance 2
        world.setTile(foodLoc, new Grass());

        int before = solo.getEnergy();
        solo.act(world); // Should move towards food

        Location after = world.getLocation(solo);
        assertEquals(1, solo.distance(after, foodLoc), "Deer should move one step towards food at distance 2");

        solo.act(world); // Should reach food and eat
        Location finalLoc = world.getLocation(solo);
        assertEquals(foodLoc, finalLoc, "Deer should reach food");
        assertTrue(solo.getEnergy() > 40, "Deer should have eaten food");
    }

    /**
     * Test that a deer wakes up in the morning after sleeping.
     */
    @Test
    void deerWakesUpInMorning() {
        world.setDay();
        Deer solo = new Deer(null);
        world.setTile(new Location(5, 5), solo);

        world.setNight();
        solo.act(world);
        assertTrue(solo.isSleeping, "Deer should be sleeping at night");

        world.setDay();
        solo.act(world);
        assertFalse(solo.isSleeping, "Deer should wake up during the day");
        assertFalse(solo.getInformation().getImageKey().contains("sleeping"), "Sprite should not be sleeping after waking up");
    }

    /**
     * Test that a deer does not move while it is sleeping.
     */
    @Test
    void deerDoesNotMoveWhileSleeping() {
        world.setDay();
        Location home = new Location(5, 5);
        Deer solo = new Deer(null);
        world.setTile(home, solo);

        world.setNight();
        solo.act(world);
        assertTrue(solo.isSleeping, "Deer should be sleeping at night");

        Location locBefore = world.getLocation(solo);
        // Act multiple times during night, should stay put
        solo.act(world);
        solo.act(world);

        Location locAfter = world.getLocation(solo);
        assertEquals(locBefore, locAfter, "Deer should not move while sleeping");
    }

    /**
     * Test that a deer sleeps even if it is far from home when night falls.
     */
    @Test
    void deerSleepsEvenIfFarFromHomeAtNight() {
        world.setDay();
        Location start = new Location(10, 10);
        Deer solo = new Deer(null);
        world.setTile(start, solo);

        // It's night, but deer is far from home
        world.setNight();
        
        solo.act(world);

        assertTrue(solo.isSleeping, "Deer should sleep at night even if far from home");
        Location locAfter = world.getLocation(solo);
        assertEquals(start, locAfter, "Deer should not have moved once it's night");
    }

    /**
     * Test that a solo deer (without a herd) does not reproduce at night.
     */
    @Test
    void soloDeerDoesNotReproduceAtNight() {
        world.setDay();
        Location p1 = new Location(5, 5);
        Location p2 = new Location(5, 6);
        
        // Deer without a herd
        Deer d1 = new Deer(null);
        Deer d2 = new Deer(null);
        
        world.setTile(p1, d1);
        world.setTile(p2, d2);
        
        d1.setEnergy(100);
        d2.setEnergy(100);
        
        // Ensure they are adults
        for (int i = 0; i < 35; i++) {
            d1.act(world);
            d2.act(world);
            d1.setEnergy(100);
            d2.setEnergy(100);
        }
        
        world.setNight();
        
        long countBefore = world.getEntities().keySet().stream().filter(o -> o instanceof Deer).count();
        d1.act(world);
        long countAfter = world.getEntities().keySet().stream().filter(o -> o instanceof Deer).count();
        
        assertEquals(countBefore, countAfter, "Solo deer (no herd) should not reproduce");
    }

    /**
     * Test that deer in a herd can reproduce at night if they are adult and have enough energy,
     * even if they are not physically close to each other.
     */
    @Test
    void deerReproducesAtNightWithoutProximity() {
        world.setDay();
        
        // Use fresh herd with 2 members
        Herd reproductionHerd = new Herd();
        
        Deer d1 = new Deer(reproductionHerd);
        Deer d2 = new Deer(reproductionHerd);
        
        // Place them far apart
        Location p1 = new Location(1, 1);
        Location p2 = new Location(10, 10);
        
        world.setTile(p1, d1);
        world.setTile(p2, d2);
        
        d1.setEnergy(100);
        d2.setEnergy(100);
        
        // Ensure they are adults
        for (int i = 0; i < 35; i++) {
            d1.act(world);
            d2.act(world);
            d1.setEnergy(100);
            d2.setEnergy(100);
        }
        
        world.setNight();
        
        int initialMembers = reproductionHerd.getMembers().size();
        assertEquals(2, initialMembers);
        
        d1.act(world);
        
        assertTrue(reproductionHerd.getMembers().size() > initialMembers, "Deer in a herd should reproduce even if far apart");
    }

    /**
     * Test that deer do not reproduce when the herd is already at its maximum capacity (10 members).
     */
    @Test
    void deerDoesNotReproduceWhenHerdIsFull() {
        world.setDay();
        Location home = new Location(5, 5);
        
        Herd fullHerd = new Herd();
        fullHerd.setHome(home);
        
        Deer d1 = null;
        for (int i = 0; i < 10; i++) {
            Deer d = new Deer(fullHerd);
            if (i == 0) d1 = d;
        }
        assertEquals(10, fullHerd.getMembers().size());
        
        world.setTile(home, d1);
        d1.setEnergy(100);
        world.setNight();
        
        d1.act(world);
        
        assertEquals(10, fullHerd.getMembers().size(), "Deer should not reproduce when herd size is 10");
    }

    /**
     * Test that a deer can reproduce after sleeping at night, which restores its energy
     * above the reproduction threshold even if it was initially too low.
     */
    @Test
    void deerReproducesAfterSleepingEvenIfEnergyWasLow() {
        world.setDay();
        Location p1 = new Location(5, 5);
        Location p2 = new Location(5, 6);
        
        Herd reproductionHerd = new Herd();
        Deer d1 = new Deer(reproductionHerd);
        Deer d2 = new Deer(reproductionHerd);
        
        world.setTile(p1, d1);
        world.setTile(p2, d2);
        
        // Energy < 30 (Animal.reproduce requirement)
        d1.setEnergy(10);
        d2.setEnergy(100);
        
        // Ensure d1 and d2 are adults
        for (int i = 0; i < 35; i++) {
            d1.act(world);
            d2.act(world);
            d1.setEnergy(100);
            d2.setEnergy(100);
        }
        d1.setEnergy(10); // set d1 back to low energy
        
        world.setNight();
        
        int initialMembers = reproductionHerd.getMembers().size();
        
        d1.act(world); // calls nightBehaviour -> sleep (energy +50) -> reproduce
        
        assertTrue(d1.getEnergy() > 10, "Energy should have increased after sleep");
        assertTrue(reproductionHerd.getMembers().size() > initialMembers, "Deer should have reproduced after sleeping despite starting with low energy");
    }
}

