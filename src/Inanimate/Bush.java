package Inanimate;

import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.Random;
import java.util.Set;

public class Bush extends Landscape {
    /* der vælges at anse Inanimate.Bush som et NonBlocking objekt, da et dyr ville kunne gå igennem en busk,
    så Actor af Actors.Animal-klassen ville kunne gå igennem en busk.
    Ydermere anvendes logik fra Inanimate.Grass-klassen til at styre den tilfældige spredning. */

    private final Random random;


    private int berry = 0;                  // antal bær på denne busk
    private final int maxBerries = 100;
    private final int berryGrowthInterval = 10; // antal ticks mellem vækst
    private int ticksSinceLastGrowth = 0;   // tæller til vækst

    // default konstruktør til Tests;
    public Bush() {
        this(new Random());
    }

    public Bush(Random random) {
        this.random = random;
    }

    @Override
    protected int spreadChance() {
        return 3;
    }

    @Override
    protected Landscape createNewInstance() {
        return new Bush();
    }
    
    @Override
    protected void afterAct(World world) {
        ticksSinceLastGrowth++;
        if (ticksSinceLastGrowth == berryGrowthInterval) {
            ticksSinceLastGrowth = 0;
            produceBerries();
        }
    }

    public void produceBerries(){
        if (berry < maxBerries) {
            berry++; // hver gang metoden kaldes, så vokser der 1 Berry på busken indtil maxBerries er nået
        }
    }

    public boolean hasBerries() {
        if (berry > 0) { // hvis der er 1 eller flere Berry så har busken et bær
            return true;
        } else {
            return false;
        }
    }

    public int getBerryCount(){
        return berry;
    }

    public void berriesEaten() {
        berry = 0;
    }

    @Override
    public DisplayInformation getInformation() {
        if (hasBerries()) {
            return new DisplayInformation(Color.GRAY, "bush-berries");
        } else {
            return new DisplayInformation(Color.DARK_GRAY, "bush");
        }
    }




}
