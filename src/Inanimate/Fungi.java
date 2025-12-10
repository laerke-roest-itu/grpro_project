package Inanimate;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.NonBlocking;
import itumulator.world.World;

import java.awt.*;
import java.util.Set;

public class Fungi implements Actor, NonBlocking, DynamicDisplayInformationProvider {
    private int lifespan;
    private World world;

    public Fungi(int lifespan) {
        this.lifespan = lifespan;
        // Constructor implementation (if needed)
    }

    @Override
    public void act(World world) {
        lifespan --;
        if (lifespan <= 0) {
            world.delete(this); // svampen dør
        }

        Set<Location> tilesInInfectionRadius = getInfectionArea(world);

        for (Location tileInRadius : tilesInInfectionRadius) {
            if (world.getTile(tileInRadius) instanceof Carcass carcass)
                infectNearbyCarcass(tileInRadius, world, carcass);
        }
    }

    public void infectNearbyCarcass (Location new_fungiLocation, World world, Carcass carcass) {
        carcass.infectWithFungi();
    }

    public Set<Location> getInfectionArea(World world) {
        int radius = 2;
        return world.getSurroundingTiles(getFungiLocation(world), radius);
    }

    public Location getFungiLocation(World world) {
        return world.getLocation(this);
    }

    // Getter for lifespan til tests
    public int getLifespan() {
        return lifespan;
    }

    @Override
    public DisplayInformation getInformation() {
        if (world.getTile(getFungiLocation(world)) instanceof Carcass) {
            return null; // svampen er skjult i ådslet
        } else {
            if (getLifespan() >= 30) { //overvej at koble til meatvalue af ådslet
                return new DisplayInformation(Color.DARK_GRAY, "fungi");
            } else {
                return new DisplayInformation(Color.LIGHT_GRAY, "fungi-small");
            }
        }
    }

    // Fungi properties and methods would go here

    // K3-2a. Udover at ådsler nedbrydes, så hjælper svampene til. Således kan der opstå svampe I et ådsel
    // Dette kan ikke ses på selve kortet, men svampen lever I selve ådslet.
    //Når ådslet er nedbrudt (og forsvinder), kan den ses som en svamp placeret på kortet, der hvor ådslet lå
    // For at læse inputfilerne skal du sikre dig, at et ådsel kan indlæses med svamp (foregår i main)

    //- K3-2b. Svampe kan kun overleve, hvis der er andre ådsler den kan sprede sig til i
    //nærheden. Er dette ikke tilfældet, vil svampen også dø efter lidt tid. Desto større ådslet
    //er, desto længere vil svampen leve efter ådslet er væk. Da svampen kan udsende
    //sporer, kan den række lidt længere end kun de omkringliggende pladser.
}
