import Actors.*;
import Inanimate.*;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;

/**
 * Main class for the ITUmulator simulation program.
 * It reads an input file to set up the world with various objects and starts the simulation.
 *
 * A ClassLoader is used to load the input file from the resources, instead of a FileReader to keep the project self-contained.
 */

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        InputStream is = Main.class
                .getClassLoader()
                .getResourceAsStream("input_files/tf4-MAX-test.txt");

        if (is == null) {
            throw new FileNotFoundException("Inputfil ikke fundet");
        }

        Scanner scanner = new Scanner(is);


        // Random used in later in both amount (in min-max) or random positions of actors
        Random random = new Random();

        // First number in the file; size of the World N
        int size = scanner.nextInt();

        // Constant parameters for the program (adjustable as needed) initialized starting values.
        int delay = 1000;
        int display_size = 800;

        Location territoryCenter = null;

        // Create ITUmulator-program with the read size
        Program program = new Program(size, display_size, delay);
        // Get world (World) out of the program - this is where we place objects
        World world = program.getWorld();

        // read the rest of the file line by line (really token by token)
        while (scanner.hasNext()) {

            String type = scanner.next();
            String amount = scanner.next();

            int count;

            // ─────────────────────────────────────
            // 1) AMOUNT (static or min-max)
            // ─────────────────────────────────────
            if (amount.contains("-")) {
                String[] parts = amount.split("-");
                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
                count = min + random.nextInt(max - min + 1);
            } else {
                count = Integer.parseInt(amount);
            }

            if (type.equals("bear")) {
                if (scanner.hasNext("\\(.*\\)")) {
                    String coords = scanner.next();         // "(3,5)"
                    coords = coords.substring(1, coords.length() - 1);
                    String[] p = coords.split(",");
                    territoryCenter = new Location(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
                } else if (territoryCenter == null) {
                    // if no center is put, choose randomly
                    territoryCenter = new Location(random.nextInt(size), random.nextInt(size));
                }
            }

            if (type.equals("deer")) {
                territoryCenter = new Location(random.nextInt(size), random.nextInt(size));
            }

            // ─────────────────────────────────────
            // 2) EXTRA FLAGS PER LINE
            // ─────────────────────────────────────
            boolean carcassHasFungi = false;

            if (type.equals("carcass") && scanner.hasNext("fungi")) {
                scanner.next();           // eat token "fungi"
                carcassHasFungi = true;
            }

            // ONE pack pr. linje
            Pack wolfPack = null;
            Herd deerHerd = null;

            if (type.equals("wolf")) {
                wolfPack = new Pack();
            }
            if (type.equals("deer")) {
                deerHerd = new Herd();
            }

            // ─────────────────────────────────────
            // 3) SPAWN count OBJECTS
            // ─────────────────────────────────────
            for (int i = 0; i < count; i++) {

                Location l;
                do {
                    l = new Location(random.nextInt(size), random.nextInt(size));
                } while (
                        (type.equals("grass") || type.equals("fungi"))
                                ? world.containsNonBlocking(l)
                                : !world.isTileEmpty(l)
                );

                switch (type) {

                    case "grass" -> world.setTile(l, new Grass());

                    case "fungi" -> world.setTile(l, new Fungi(100));

                    case "bush" -> world.setTile(l, new Bush());

                    case "burrow" -> world.setTile(l, new Burrow());

                    case "carcass" -> {
                        if (carcassHasFungi)
                            world.setTile(l, new Carcass(80, 25, true));
                        else
                            world.setTile(l, new Carcass(80, 25));
                    }

                    case "rabbit" -> world.setTile(l, new Rabbit());

                    case "bear" -> {
                        Bear bear = new Bear(territoryCenter);
                        world.setTile(l, bear);
                    }

                    case "wolf" -> {
                        Wolf wolf = new Wolf(wolfPack);
                        world.setTile(l, wolf);
                    }

                    case "deer" -> {
                        Deer deer = new Deer(deerHerd, territoryCenter);
                        world.setTile(l, deer);

                        // first deer = leader → home
                        if (i == 0) deerHerd.setHome(l);
                    }

                    default -> {
                        // unknown type in inputfile → ignore or print
                        System.out.println("Ukendt type: " + type);
                    }
                }
            }
        }

        scanner.close();

// Start simulation (GUI)
        program.show();
            for (int i = 0; i < 200; i++) {
            program.simulate();
            }
    }
}
