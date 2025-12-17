import Actors.Rabbit;
import Inanimate.Burrow;
import Inanimate.Grass;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RabbitTest {

    private World world;
    private Rabbit rabbit;
    private Location rabbitLoc;

    // Random der altid returnerer samme double (kun til burrow-chancen i Rabbit)
    static class FixedRandom extends Random {
        private final double value;
        FixedRandom(double value) { this.value = value; }
        @Override public double nextDouble() { return value; }
    }

    @BeforeEach
    void setUp() {
        world = new World(10);
        world.setDay();

        rabbitLoc = new Location(5, 5);
        rabbit = new Rabbit(new FixedRandom(0.90)); // > 0.50 => dig/claim sker ikke automatisk
        world.setTile(rabbitLoc, rabbit);
    }

    @AfterEach
    void tearDown() {
        world = null;
        rabbit = null;
        rabbitLoc = null;
    }

    // --- små helpers (ingen simulateTicks/Program) ---

    private void forceDayAndAct(Object actor, int times) {
        for (int i = 0; i < times; i++) {
            world.setDay();
            if (actor instanceof Rabbit r) r.act(world);
        }
    }

    private void forceNightAndAct(Object actor, int times) {
        for (int i = 0; i < times; i++) {
            world.setNight();
            if (actor instanceof Rabbit r) r.act(world);
        }
    }

    private void makeAdult(Rabbit r) {
        // Rabbit.isChild() => age < 10, age++ sker kun når world.isDay()
        forceDayAndAct(r, 17);
        assertFalse(r.isChild(), "Rabbit should be adult for reproduction tests");
    }

    // ------------------- TESTS -------------------

    @Test
    void rabbitDigsBurrowDuringDay_whenNoBurrow() {
        // Erstat kanin med en der ALTID digger (nextDouble = 0.10 < 0.25)
        world.delete(rabbit);
        rabbit = new Rabbit(new FixedRandom(0.0));
        world.setTile(rabbitLoc, rabbit);

        // Én dag-act er ofte nok (den kan dog flytte først, men digger bagefter)
        world.setDay();
        rabbit.act(world);

        assertNotNull(rabbit.getBurrow(), "Rabbit should dig a burrow when random < 0.25");
        assertTrue(world.contains(rabbit.getBurrow()), "Burrow should exist in world");
        assertTrue(world.isOnTile(rabbit.getBurrow()), "Burrow should be placed on the map");
    }

    @Test
    void rabbitEatsGrassAndGainsEnergy() {
        rabbit.setEnergy(10); // sulten

        // Læg grass som NON-blocking på ALLE tiles (undtagen der hvor rabbit står).
        // Så uanset hvor rabbit flytter hen, står den på grass og kan spise det.
        for (int x = 0; x < world.getSize(); x++) {
            for (int y = 0; y < world.getSize(); y++) {
                Location l = new Location(x, y);
                if (l.equals(rabbitLoc)) continue;          // rabbit står her (blocking)
                if (!world.containsNonBlocking(l)) {
                    world.setTile(l, new Grass());
                }
            }
        }

        int before = rabbit.getEnergy();

        // et par dag-acts (age/energy tick + move + eat)
        forceDayAndAct(rabbit, 5);

        assertTrue(rabbit.getEnergy() > before, "Rabbit should gain energy from eating grass");
    }

    @Test
    void rabbitSleepsAtNight_whenHasBurrow() {
        Burrow burrow = new Burrow();
        Location burrowLoc = new Location(4, 5);
        world.setTile(burrowLoc, burrow);
        rabbit.setBurrow(burrow);

        // Nat => nightBehaviour => sleep => world.remove(this)
        world.setNight();
        rabbit.act(world);

        assertTrue(world.contains(rabbit), "Rabbit should still exist in world (not deleted)");
        assertFalse(world.isOnTile(rabbit), "Sleeping rabbit should be removed from the map (in burrow)");
    }

    @Test
    void rabbitsReproduceAtNight_whenTwoInSameBurrow_andLeaderAdult() {
        Burrow burrow = new Burrow();
        Location burrowLoc = new Location(4, 5);
        world.setTile(burrowLoc, burrow);

        Rabbit leader = new Rabbit(new FixedRandom(0.90));
        Rabbit other  = new Rabbit(new FixedRandom(0.90));

        world.setTile(new Location(4, 4), leader);
        world.setTile(new Location(4, 6), other);

        leader.setBurrow(burrow);
        other.setBurrow(burrow);

        makeAdult(leader);
        makeAdult(other);

        int kidsBefore = leader.getAmountOfKids();

        leader.setEnergy(100);
        other.setEnergy(100);
        // nat => leader reproducerer (hvis 2+ i burrow og leader)
        world.setNight();
        leader.act(world);
        other.act(world);

        assertTrue(leader.getAmountOfKids() > kidsBefore,
                "Leader rabbit should reproduce at night when 2+ rabbits share a burrow and are adult");
    }
}
