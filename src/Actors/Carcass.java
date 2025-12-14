package Actors;

import Inanimate.*;
import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.Random;

public class Carcass implements Actor, DynamicDisplayInformationProvider {
    private int meatLeft;
    private int maxMeat;
    private int rotTimer;
    private boolean hasFungi;
    private Random random;
    private Fungi fungi; // til test

    // HOVEDKONSTRUKTØR
    public Carcass(int meatLeft, int rotTimer, boolean hasFungi, Random random) {
        this.meatLeft = meatLeft;
        this.rotTimer = rotTimer;
        maxMeat = meatLeft;
        this.hasFungi = hasFungi;
        this.random = random;
    }

    // normal brug
    public Carcass(int meatLeft, int rotTimer) {
        this(meatLeft, rotTimer, false, new Random());
    }

    // carcass med svamp (fx fra inputfil)
    public Carcass(int meatLeft, int rotTimer, boolean hasFungi) {
        this(meatLeft, rotTimer, hasFungi, new Random());
    }

    // konstruktør til tests
    public Carcass(Random random) {
        this(10, 10, false, random); // testværdier
    }

    @Override
    public void act(World world) {
        if (hasFungi) {
            rotTimer--; // hvis der er svampe, så forrådner ådslet hurtigere
        }
        rotTimer--;
        trySpawnFungi();

        if (rotTimer <= 0 || meatLeft <= 0) {
            Location myLoc = world.getLocation(this);

            // gemmer om der var svamp, før vi sletter ådslet
            boolean spawnFungi = hasFungi;

            // ådslet forsvinder altid
            world.delete(this);

            // hvis ingen svamp → færdig
            if (!spawnFungi || myLoc == null) {
                return;
            }

            // tjek hvad der står som non-blocking på feltet
            Object nb = world.getNonBlocking(myLoc);

            if (nb == null) {
                // ingen non-blocking → bare placer svamp
                world.setTile(myLoc, new Fungi(calculateFungiLifespan()));

            } else if (nb instanceof Grass) {
                // må gerne overskrive grass
                world.delete(nb);
                world.setTile(myLoc, new Fungi(calculateFungiLifespan()));

            } else {
                // fx Burrow eller noget andet non-blocking → gør ingenting
                // (ingen svamp på kortet, men logikken er stadig ok)
            }
        }
    }

    private void trySpawnFungi() {
        if (hasFungi) return; // hvis der allerede er en fungi, så gør intet

        // 5% chance per tick for at spawne en fungi
        double chance = 0.05;

        if (random.nextDouble() < chance) {
            hasFungi = true;
        }
    }

    public void infectWithFungi() {
        if (hasFungi) {
            return; // allerede inficeret
        }
        hasFungi = true;
    }

    public void eaten(int amount) {
        meatLeft -= amount;
        if (meatLeft <= 0) {
            meatLeft = 0;
        }
    }

    public int getMeatLeft() {
        return meatLeft;
    }

    public void setFungi(Fungi f) {
        this.fungi = f;
    }

    public Fungi getFungi() {
        return this.fungi;
    }

    private int calculateFungiLifespan() {
        return maxMeat * 2;
    }

    @Override
    public DisplayInformation getInformation() {
        if (getMeatLeft() >= 50) {
            return new DisplayInformation(Color.GRAY, "carcass");
        } else if (getMeatLeft() >= 0 && getMeatLeft() < 50) {
            return new DisplayInformation(Color.GRAY, "carcass-small");
        } else {
            return null;
        }
    }
}

