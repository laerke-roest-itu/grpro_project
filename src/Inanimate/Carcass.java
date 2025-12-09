package Inanimate;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.simulator.Actor;
import itumulator.world.World;

import java.awt.*;

public class Carcass implements Actor, DynamicDisplayInformationProvider {
    private int meatLeft;      // hvor meget kød der er tilbage
    private int rotTimer;      // hvor mange ticks før ådslet rådner væk// hvilken art det stammer fra

    public Carcass(int meatLeft, int rotTimer) {
        this.meatLeft = meatLeft;
        this.rotTimer = rotTimer;
    }

    @Override
    public void act(World world) {
        rotTimer--;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
        if (rotTimer <= 0 || meatLeft <= 0) {
            world.delete(this); // ådslet forsvinder
        }
    }

=======
        trySpawnFungi();

        if (rotTimer <= 0 || meatLeft <= 0) {
=======
        trySpawnFungi();

        if (rotTimer <= 0 || meatLeft <= 0) {
>>>>>>> Stashed changes
=======
        trySpawnFungi();

        if (rotTimer <= 0 || meatLeft <= 0) {
>>>>>>> Stashed changes
=======
        trySpawnFungi();

        if (rotTimer <= 0 || meatLeft <= 0) {
>>>>>>> Stashed changes
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

>>>>>>> Stashed changes
    public void eaten(int amount) {
        meatLeft -= amount;
        if (meatLeft <= 0) {
            meatLeft = 0;
        }
    }

    public int getMeatLeft() {
        return meatLeft;
    }

    @Override
    public DisplayInformation getInformation() {
        if (meatLeft >= 50) {
            return new DisplayInformation(Color.GRAY, "carcass");
        } else if (meatLeft >= 0 && meatLeft < 50) {
            return new DisplayInformation(Color.GRAY, "carcass-small");
        } else {
            return null;
        }
    }
}

