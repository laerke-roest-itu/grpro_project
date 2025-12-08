package Inanimate;

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
    /* der vælges at anse Inanimate.Bush som et NonBlocking objekt, da en Actor af Actors.Animal-klassen ville kunne gå igennem en busk.
        ydermere anvendes logik fra Inanimate.Grass-klassen til at styre den tilfældige spredning. Her til bare specificeret til
        et enkelt felt fremfor alle 8 rundt om et Inanimate.Grass-felt. */

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

        // Ca. 3% chance for at busken spreder sig i dette act - lavere % chance end Inanimate.Grass-klassen, da græs spreder sig
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
    // skal implementere en metode så de her Inanimate.Bush kan få Berries
    // Actors.Bear klassen skal kunne kalde til klasse i forhold til hasBerries, produceBerries, berriesCount,

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

    // Actors.Bear skal få Energy når den Eat()-metode på Inanimate.Bush if hasBerries()
    /* i forhold til Berry skal give Energy til Actors.Bear - jeg kan have Actors.Bear-klassen til bare at give +5 Energy, hvis
        den spiser et Berry, hvis Energy-systemet findes hos Actors.Animal-superklassen, med mindre vi kan have en super-klasse
        over Actors.Animal-superklassen hvor de andre klasser kan implementere Energy.
     */

    // eventuelt kode til en Actors.Bear klasse - den behøver ikke at gøre andet end at anvende takeBerry metoden, da den der
    // ved at det bliver berry--, så den fjerner en berry fra busken.

    // // antag world.getTile(location) returnerer et objekt (evt. Inanimate.Bush)
    //Object tileObj = world.getTile(location);
    //if (tileObj instanceof Inanimate.Bush) {
    //    Inanimate.Bush bush = (Inanimate.Bush) tileObj;
    //    if (bush.hasBerries() && bush.takeBerry()) {
    //        this.increaseEnergy(5); // eller kald super.metode
    //    }
    //}



}
