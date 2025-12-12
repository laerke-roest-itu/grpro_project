import Actors.Bear;
import Actors.Rabbit;
import Actors.Wolf;
import Inanimate.Bush;
import Actors.Carcass;
import itumulator.world.World;
import itumulator.world.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

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

    // ========= Reflection-hjælpere =========

    private Field findField(Class<?> cls, String name) {
        Class<?> current = cls;
        while (current != null) {
            try {
                Field f = current.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new RuntimeException("Field " + name + " not found on " + cls);
    }

    private int getIntField(Object o, String name) {
        try {
            Field f = findField(o.getClass(), name);
            return f.getInt(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setIntField(Object o, String name, int value) {
        try {
            Field f = findField(o.getClass(), name);
            f.setInt(o, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getBoolField(Object o, String name) {
        try {
            Field f = findField(o.getClass(), name);
            return f.getBoolean(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setBoolField(Object o, String name, boolean value) {
        try {
            Field f = findField(o.getClass(), name);
            f.setBoolean(o, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getEnergy(Bear b) {
        return getIntField(b, "energy");
    }

    private void setEnergy(Bear b, int value) {
        setIntField(b, "energy", value);
    }

    private void setAge(Bear b, int value) {
        setIntField(b, "age", value);
    }

    private boolean isAlive(Bear b) {
        return getBoolField(b, "isAlive");
    }

    // ========= Hjælpere til verden =========

    private void place(Object obj, Location loc) {
        // RET HER hvis jeres World har anden metode (fx place())
        w10.setTile(loc, obj);
    }

    private int manhattan(Location a, Location b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    // ========= Tests relateret til act() =========

    @Test
    void act_bearDiesWhenTooOld() {
        Location loc = new Location(2, 2);
        place(bear, loc);

        setAge(bear, 400);   // age >= 400
        setEnergy(bear, 50); // positiv energi

        bear.act(w10);

        //assertFalse(isAlive(bear), "Bjørnen skal dø når age >= 400.");
        assertFalse(w10.contains(bear), "Bjørnen skal være fjernet fra verden når den dør.");
    }

    @Test
    void act_bearDiesWhenNoEnergy() {
        Location loc = new Location(3, 3);
        place(bear, loc);

        setEnergy(bear, 0);  // energy <= 0
        setAge(bear, 10);    // lav alder, så det er energi der tæller

        bear.act(w10);

        //assertFalse(isAlive(bear), "Bjørnen skal dø når energy <= 0.");
        assertFalse(w10.contains(bear), "Bjørnen skal være fjernet fra verden når den dør.");
    }

    @Test
    void act_whenOutsideTerritoryAndDay_movesTowardsTerritoryCenter() {
        // Denne test antager at verden starter i "dag"-tilstand
        // (hvis den ikke gør, må du sætte tiden i World til dag med jeres API).

        Location start = new Location(0, 0);
        place(bear, start);

        setAge(bear, 10);
        setEnergy(bear, 100); // så den ikke dør og ikke er sulten nok til andet sjov

        int distBefore = manhattan(start, territoryCenter);

        bear.act(w10);

        Location after = w10.getLocation(bear);
        assertNotNull(after, "Bjørnen skal have en position efter act().");
        assertNotEquals(start, after, "Bjørnen skal have bevæget sig.");

        // ét skridt ad gangen
        var neighbors = w10.getSurroundingTiles(start);
        assertTrue(neighbors.contains(after),
                "Bjørnen skal kun bevæge sig ét skridt pr. tur.");

        int distAfter = manhattan(after, territoryCenter);
        assertTrue(distAfter < distBefore,
                "Bjørnen skal være tættere på territoriecenteret efter act().");
    }

    @Test
    void act_whenHungryAndCarcassAdjacent_eatsFromCarcass() {
        // antager dag og indenfor territoriet → bjørn kalder hunt()

        Location bearLoc = territoryCenter;
        place(bear, bearLoc);

        // sulten men i live
        setEnergy(bear, 10); // > 0 men lav → isHungry() bør være true
        setAge(bear, 10);

        // carcass ved siden af
        Location carcassLoc = new Location(territoryCenter.getX() + 1, territoryCenter.getY());
        Carcass carcass = new Carcass(50,25);      // RET HER hvis Carcass har anden ctor
        place(carcass, carcassLoc);

        int energyBefore = getEnergy(bear);
        int meatBefore = carcass.getMeatLeft();

        bear.act(w10); // Husk at tage højde for at act koster én energi

        int energyAfter = getEnergy(bear);
        int meatAfter = carcass.getMeatLeft();

        int meatEaten = meatBefore - meatAfter;
        assertTrue(meatEaten <= 30 && meatEaten >= 0,
                "Bjørnen må højst spise 30 kød ad gangen fra Carcass.");

        assertEquals(energyBefore + meatEaten - 1, energyAfter,
                "Bjørnens energi skal stige med samme mængde kød som den spiser.");
    }

    // ========= Direkte tests af eat / canEat / getFoodEnergy =========

    @Test
    void eatCarcassDirectly_increasesEnergyAndReducesMeat() {
        Location loc = new Location(4, 4);
        Carcass carcass = new Carcass(50,25);      // RET HER hvis ctor er anderledes
        place(carcass, loc);

        setEnergy(bear, 5);
        int energyBefore = getEnergy(bear);
        int meatBefore = carcass.getMeatLeft();

        bear.eat(w10, loc);

        int energyAfter = getEnergy(bear);
        int meatAfter = carcass.getMeatLeft();

        int meatEaten = meatBefore - meatAfter;
        assertTrue(meatEaten <= 30 && meatEaten >= 0,
                "Bjørnen må højst spise 30 kød ad gangen fra Carcass.");

        assertEquals(energyBefore + meatEaten, energyAfter,
                "Energi skal øges med mængden bjørnen spiser.");
    }

    @Test
    void eatBushWithBerries_givesDoubleEnergyPerBerry() {
        Location loc = new Location(6, 6);
        Bush bush = new Bush();
        bush.produceBerries();
        place(bush, loc);

        setEnergy(bear, 10);
        int energyBefore = getEnergy(bear);

        int berriesBefore = bush.getBerryCount();
        assertTrue(berriesBefore > 0,
                "Denne test antager at bush starter med mindst ét bær.");

        bear.eat(w10, loc);

        int energyAfter = getEnergy(bear);
        int berriesAfter = bush.getBerryCount();

        assertEquals(energyBefore + berriesBefore * 2, energyAfter,
                "Bjørnen skal få 2 energi pr. bær i busken.");
        assertTrue(berriesAfter < berriesBefore,
                "Buskens antal bær skal falde efter bjørnen har spist.");
    }

    @Test
    void canEatOnlyCarcassOrBush() {
        Object carcass = new Carcass(50,25);
        Object bush = new Bush();
        Object rabbit = new Rabbit();
        Object wolf = new Wolf(null);
        Object somethingElse = new Object();

        assertTrue(bear.canEat(carcass), "Bear skal kunne spise Carcass.");
        assertTrue(bear.canEat(bush), "Bear skal kunne spise Bush.");
        assertFalse(bear.canEat(rabbit), "Bear må ikke spise Rabbit direkte.");
        assertFalse(bear.canEat(wolf), "Bear må ikke spise Wolf direkte (kun Carcass/Bush).");
        assertFalse(bear.canEat(somethingElse), "Bear må ikke spise vilkårlige objekter.");
    }

    @Test
    void getFoodEnergyReturnsCorrectValues() {
        Rabbit rabbit = new Rabbit();
        Wolf wolf = new Wolf(null);
        Bush bush = new Bush();
        Object other = new Object();

        assertEquals(40, bear.getFoodEnergy(rabbit), "Rabbit skal give 40 energi.");
        assertEquals(60, bear.getFoodEnergy(wolf), "Wolf skal give 60 energi.");
        assertEquals(10, bear.getFoodEnergy(bush), "Bush skal give 10 energi pr. bær.");
        assertEquals(0, bear.getFoodEnergy(other), "Andre objekter skal give 0 energi.");
    }

    // ========= Fjender / kamp =========

    @Test
    void isEnemyPredatorWolvesAndOtherBearsAreEnemies() {
        Bear bear1 = bear;
        Bear bear2 = new Bear(territoryCenter);
        Wolf wolf = new Wolf(null);
        Rabbit rabbit = new Rabbit(); // ikke Predator

        assertFalse(bear1.isEnemyPredator(rabbit),
                "Rabbit er ikke Predator → ikke fjende.");
        assertTrue(bear1.isEnemyPredator(wolf),
                "Wolf skal være fjende til Bear.");
        assertFalse(bear1.isEnemyPredator(bear1),
                "En bjørn er ikke fjende med sig selv.");
        assertTrue(bear1.isEnemyPredator(bear2),
                "Andre bjørne skal ses som fjender ifølge implementeringen.");
    }

    @Test
    void getAttackDamageIs25() {
        assertEquals(25, bear.getAttackDamage(),
                "getAttackDamage() skal returnere 25 for Bear.");
    }
}
