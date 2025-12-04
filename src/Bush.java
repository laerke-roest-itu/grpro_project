import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.NonBlocking;
import itumulator.world.World;

import java.awt.*;
import java.util.Random;
import java.util.Set;

public class Bush implements Actor, NonBlocking, DynamicDisplayInformationProvider {
    /* der vælges at anse Bush som et NonBlocking objekt, da en Actor af Animal-klassen ville kunne gå igennem en busk.
        ydermere anvendes logik fra Grass-klassen til at styre den tilfældige spredning. */

    private final Random random;

    private int berry = 0;                  // antal bær på denne busk
    private final int maxBerries = 100;
    private boolean hasBerries;
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
    public void act(World world) {
        ticksSinceLastGrowth++;
        if (ticksSinceLastGrowth == berryGrowthInterval) {
            ticksSinceLastGrowth = 0;
            produceBerries();
        }

        // Finder buskens nuværende position i verden
        Location bushLocation = world.getLocation(this);

        // Henter alle nabofelter
        Set<Location> neighbours = world.getSurroundingTiles(bushLocation);

        // Ca. 3% chance for at busken spreder sig i dette act - lavere % chance end Grass-klassen, da græs spreder sig
        // hurtigere end buske.
        if (random.nextInt(100) <= 3) {

            // Gennemgå alle nabofelter ét ad gangen
            for (Location neighbourTile : neighbours) {

                // Tjekker om der INGEN non-blocking objekter findes på dette felt
                // (dvs. der står ikke allerede græs eller noget andet non-blocking)
                if (!world.containsNonBlocking(neighbourTile)) {

                    // Placér nye buske på alle nabofelter
                    // hvis ikke der allerede er non-blocking objekter
                    world.setTile(neighbourTile, new Bush());

                    // har prøvet at implementere en random ud fra de 8 felter med NonBlocking elementer. Om det virker
                    // det ved jeg ikke.
                }
            }
        }
    }
    // skal implementere en metode så de her Bush kan få Berries
    // Bear klassen skal kunne kalde til klasse i forhold til hasBerries, produceBerries, berriesCount,

    public void produceBerries(){
        if (berry < maxBerries) {
            berry++; //usikker på om den bare starter ud med at lave 2 eller ej, det skal måske også være => men der
            // kommer måske en logisk fejl hvis den bliver = maxBerries og så forsøger at lave et berry til. > er nok
            // det bedste valg.
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


    // kan være jeg bare kan fjerne den her, der var noget samtale med Theodor der havde lavet et andet system i Bear
    // klassen. Mindes jeg gjorde det men der var problemer da jeg pullede fra main og nogle ændringer blev rodede.
    public boolean takeBerry() {
        if (berry > 0) {
            berry--;
            return true;
        }
        return false;
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
