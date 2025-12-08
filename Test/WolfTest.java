import Actors.Bear;
import Actors.Rabbit;
import Actors.Wolf;
import Inanimate.Carcass;
import Inanimate.Pack;
import itumulator.world.World;
import itumulator.world.Location;
import itumulator.executable.DisplayInformation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class WolfTest {

    private World w10;
    private Pack pack;
    private Wolf wolf;

    @BeforeEach
    void setUp() {
        w10 = new World(10);
        pack = new Pack();               // RET hvis Pack kræver andre argumenter
        wolf = new Wolf(pack);
    }

    @AfterEach
    void tearDown() {
        w10 = null;
        wolf = null;
        pack = null;
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

    private int getEnergy(Wolf w) {
        return getIntField(w, "energy");
    }

    private void setEnergy(Wolf w, int value) {
        setIntField(w, "energy", value);
    }

    private void setAge(Wolf w, int value) {
        setIntField(w, "age", value);
    }

    private boolean isAlive(Wolf w) {
        return getBoolField(w, "isAlive");
    }

    // ========= Hjælpere til verden =========

    private void place(Object obj, Location loc) {
        // RET HER hvis jeres World bruger en anden metode
        w10.setTile(loc, obj);
    }

    // ========= Tests relateret til act()-logikken =========

    @Test
    void act_wolfDiesWhenTooOld() {
        Location loc = new Location(2, 2);
        place(wolf, loc);

        // age >= 240 → ulven skal dø i act()
        setAge(wolf, 239);
        setEnergy(wolf, 50); // positiv energi, så det er alderen der udløser det

        wolf.act(w10);

        // Forvent at ulven ikke længere er i live og (oftest) fjernet fra verden
        //assertFalse(isAlive(wolf), "Ulven skal være død når age >= 240.");
        assertFalse(w10.contains(wolf), "Ulven skal være fjernet fra verden når den dør.");
    }

    @Test
    void act_wolfDiesWhenNoEnergy() {
        Location loc = new Location(3, 3);
        place(wolf, loc);

        // energy <= 0 → ulven skal dø
        setEnergy(wolf, 1);
        setAge(wolf, 10); // lav alder så det er energi der udløser det

        wolf.act(w10);

        //assertFalse(isAlive(wolf), "Ulven skal dø når energy <= 0.");
        assertFalse(w10.contains(wolf), "Ulven skal være fjernet fra verden når den dør.");
    }

    // ========= Metoder der bruges via jagt/overlevelse =========

    @Test
    void isChildTrueWhenAgeUnder40() {
        setAge(wolf, 0);
        assertTrue(wolf.isChild(), "isChild() skal være true når age < 40.");

        setAge(wolf, 39);
        assertTrue(wolf.isChild(), "isChild() skal være true når age < 40.");
    }

    @Test
    void isChildFalseWhenAgeAtLeast40() {
        setAge(wolf, 40);
        assertFalse(wolf.isChild(), "isChild() skal være false når age >= 40.");
    }

    @Test
    void canEatOnlyCarcass() {
        Object carcass = new Carcass(50,25);    // RET HER hvis Carcass kræver andre argumenter
        Object rabbit = new Rabbit();
        Object bear = new Bear(new Location(0, 0));
        Object somethingElse = new Object();

        assertTrue(wolf.canEat(carcass), "Wolf skal kunne spise Carcass.");
        assertFalse(wolf.canEat(rabbit), "Wolf må ikke spise Rabbit direkte (kun Carcass).");
        assertFalse(wolf.canEat(bear), "Wolf må ikke spise Bear.");
        assertFalse(wolf.canEat(somethingElse), "Wolf må ikke spise vilkårlige objekter.");
    }

    @Test
    void eatFromCarcassIncreasesEnergyAndReducesMeat() {
        Location loc = new Location(4, 4);
        Carcass carcass = new Carcass(50,25);       // RET HER hvis konstruktør er anderledes
        place(carcass, loc);

        // sæt ulven et sted i verden
        place(wolf, new Location(1, 1));

        // giv ulven lidt startenergi
        setEnergy(wolf, 10);
        int energyBefore = getEnergy(wolf);

        int meatBefore = carcass.getMeatLeft();

        wolf.eat(w10, loc);

        int energyAfter = getEnergy(wolf);
        int meatAfter = carcass.getMeatLeft();

        int meatEaten = meatBefore - meatAfter;
        assertTrue(meatEaten <= 20 && meatEaten >= 0,
                "Ulven må højst spise 20 kød ad gangen fra Carcass.");

        assertEquals(energyBefore + meatEaten, energyAfter,
                "Ulvens energi skal stige med samme mængde kød som den spiser.");
    }

    // ========= Fjender / kamp =========

    @Test
    void isEnemyPredatorDifferentPackWolvesAndBearsAreEnemies() {
        // Denne ulv er i pack1
        Pack pack1 = new Pack();
        Wolf wolf1 = new Wolf(pack1);

        // Ulv i samme pack → ikke fjende
        Wolf samePackWolf = new Wolf(pack1);

        // Ulv i anden pack → fjende
        Pack pack2 = new Pack();
        Wolf otherPackWolf = new Wolf(pack2);

        // Bjørn → fjende
        Bear bear = new Bear(new Location(0, 0));

        assertFalse(wolf1.isEnemyPredator(samePackWolf),
                "Ulv i samme pack skal ikke være fjende.");
        assertTrue(wolf1.isEnemyPredator(otherPackWolf),
                "Ulv i anden pack skal være fjende.");
        assertTrue(wolf1.isEnemyPredator(bear),
                "Bear skal være fjende til Wolf.");
    }

    @Test
    void getAttackDamageIs10() {
        assertEquals(10, wolf.getAttackDamage(),
                "getAttackDamage() skal returnere 10.");
    }

    // (Valgfrit) Man kan også teste getInformation(), men det er mest visuel logik
    // og ikke strengt nødvendig ift. act().

}


