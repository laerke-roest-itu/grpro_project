import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.NonBlocking;
import itumulator.world.World;

import java.util.Random;
import java.util.Set;

public class Bush implements Actor, NonBlocking {
    /* der vælges at anse Bush som et NonBlocking objekt, da en Actor af Animal-klassen ville kunne gå igennem en busk.
        ydermere anvendes logik fra Grass-klassen til at styre den tilfældige spredning. Her til bare specificeret til
        et enkelt felt fremfor alle 8 rundt om et Grass-felt.
     */

    //jeg skal have mig noget display metode i forhold til at bruge billedbiblioteket af hvordan en Bush ser ud


    private final Random random;

    private int berry = 0;                  // antal bær på denne busk
    private final int maxBerries = 3;       // cap for bær
    private final int berryGrowthInterval = 10; // antal ticks mellem vækst (juster efter behov)
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
        // to forskellige måder at håndtere growth, her er det så i forhold til ticks, men hvor kan jeg tjekke de her
        // ticks og referere til dem?
        ticksSinceLastGrowth++;
        if (ticksSinceLastGrowth >= berryGrowthInterval) {
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

                    // Placér nyt græs på alle nabofelter
                    // hvis ikke der allerede er non-blocking objekter
                    world.setTile(neighbourTile, new Bush(this.random));

                    // har prøvet at implementere en random ud fra de 8 felter med NonBlocking elementer. Om det virker
                    // det ved jeg ikke.
                }
            }
        }

    }
    // skal implementere en metode så de her Bush kan få Berries
    // Bear klassen skal kunne kalde til klasse i forhold til hasBerries, produceBerries, berriesCount,

    public void produceBerries(){
        if (berry > maxBerries) {
            berry++; // producer et bær om dagen, det gør den så en gang om dagen
            // er nok et problem her med den lige nu vil have et bær + 1 til at starte med.
        }
    }

    public boolean hasBerries(){
        return berry > 0; // hvis der er 1 eller flere Berry så har busken et bær
    }

    public int getBerryCount(){
        return berry;

        // ville jeg lave en hashmap her? hvordan laver jeg en tæller for hvor mange bær der er
        // hvor mange vil jeg have, kan jeg cappe den af ved at have max 1? I tilfælde hvordan ville jeg så gøre det.
        // det mest simple ville være at "if dag berry++" eller hvad man kalder det. Der skal i hvert fald laves 1
        // berry.
        // if Bear eat() Berry, berry-1. Her skal jeg så også have en nedre grænse. Jeg har lyst til at sige hvad med
        // noget rekursion? Skal i hvert fald have noget der fortæller at Berry ikke må være >0.
    }

    public boolean takeBerry() {
        if (berry > 0) {
            berry--;
            return true;
        }
        return false;
    }


    // Bear skal få Energy når den Eat()-metode på Bush if hasBerries()
    /* i forhold til Berry skal give Energy til Bear - jeg kan have Bear-klassen til bare at give +5 Energy, hvis
        den spiser et Berry, hvis Energy-systemet findes hos Animal-superklassen, med mindre vi kan have en super-klasse
        over Animal-superklassen hvor de andre klasser kan implementere Energy.
     */

    // eventuelt kode til en Bear klasse - den behøver ikke at gøre andet end at anvende takeBerry metoden, da den der
    // ved at det bliver berry--, så den fjerner en berry fra busken.

    // // antag world.getTile(location) returnerer et objekt (evt. Bush)
    //Object tileObj = world.getTile(location);
    //if (tileObj instanceof Bush) {
    //    Bush bush = (Bush) tileObj;
    //    if (bush.hasBerries() && bush.takeBerry()) {
    //        this.increaseEnergy(5); // eller kald super.metode
    //    }
    //}

}
