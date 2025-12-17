package Inanimate;

import itumulator.executable.DisplayInformation;
import itumulator.world.World;

import java.awt.*;
import java.util.Random;


public class Bush extends Landscape {
    //private final Random random;
    private int berry = 0;                  // antal bær på denne busk
    private final int maxBerries = 100;
    private final int berryGrowthInterval = 10; // antal ticks mellem vækst
    private int ticksSinceLastGrowth = 0;   // tæller til vækst

    // normal brug i simulator
    public Bush() {
        this(new Random());
    }

    // test-brug: styr tilfældighed
    public Bush(Random random) {
        super();
        this.random = random;
    }

    @Override
    protected int spreadChance() { //spreadChance fra superklassen gives en værdi
        return 3;
    }

    @Override
    protected Landscape createNewInstance() { // createNewInstance fra superklassen gives en instans af Bush
        return new Bush();
    }
    
    @Override
    protected void afterAct(World world) {
        ticksSinceLastGrowth++;
        if (ticksSinceLastGrowth == berryGrowthInterval) { // når der er gået nok ticks siden sidste vækst laves
            ticksSinceLastGrowth = 0;                       // et berry og tælleren nulstilles
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
