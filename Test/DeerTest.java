import Actors.Bear;
import Actors.Deer;
import Inanimate.Grass;
import Inanimate.Herd;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class DeerTest {

    private Program program;
    private World world;

    private Herd herd;
    private Deer leader;
    private Deer member;

    private Location leaderLoc;
    private Location memberLoc;

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

    @Test
    void leaderSetsHerdHomeOnFirstAct() {
        assertNull(herd.getHome(), "Home should start as null");

        leader.act(world);

        assertEquals(leaderLoc, herd.getHome(), "Leader should set herd home to its starting position");
    }

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

    /** Test that a deer seeks shelter at dusk (totalDayDuration - 3) by moving toward its herd's home location.
     */

    @Test
    void deerSeeksShelterAtDusk() {
        // make sure herd home is set

        leader.act(world);


        /*
        // moves member far from home, so it has to move toward it
        world.delete(member);
        member = new Deer(herd);
        Location far = new Location(10, 10);
        world.setTile(far, member);
        */

        // set time to "Dusk": totalDayDuration - 3
        world.setCurrentTime(World.getTotalDayDuration() - 3);

        Location before = world.getLocation(member);
        member.act(world); // Herbivore.act -> seekShelter() -> moveOneStepTowards(home)

        Location after = world.getLocation(member);
        assertNotEquals(before, after, "Deer should move toward home at dusk");
    }

    /** Test that a deer eats grass and gains energy during the day when hungry.
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

}

