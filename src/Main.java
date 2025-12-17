import Actors.*;
import Inanimate.*;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        InputStream is = Main.class
                .getClassLoader()
                .getResourceAsStream("input_files/tf4-MAX_Integer.txt");

        if (is == null) {
            throw new FileNotFoundException("Inputfil ikke fundet");
        }

        Scanner scanner = new Scanner(is);


        // Random bruges senere til både antal (ved min-max) og tilfældige positioner
        Random random = new Random();

        // Første tal i filen: verdens størrelse N
        int size = scanner.nextInt();

        // Konstante parametre til programmet (kan justeres efter behov)
        int delay = 1000;
        int display_size = 800;

        Location territoryCenter = null;

        // Opret ITUmulator-programmet med den læste størrelse
        Program program = new Program(size, display_size, delay);
        // Hent verden (World) ud af programmet – det er her vi placerer objekter
        World world = program.getWorld();

        // Læs resten af filen linje for linje (egentlig token for token)
        while (scanner.hasNext()) {

            String type = scanner.next();
            String amount = scanner.next();

            int count;

            // ─────────────────────────────────────
            // 1) ANTAL (fast eller min-max)
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
                    // hvis ingen center er sat endnu, vælg et tilfældigt
                    territoryCenter = new Location(random.nextInt(size), random.nextInt(size));
                }
            }

            // ─────────────────────────────────────
            // 2) EKSTRA FLAGS PR. LINJE
            // ─────────────────────────────────────
            boolean carcassHasFungi = false;

            if (type.equals("carcass") && scanner.hasNext("fungi")) {
                scanner.next();           // spis token "fungi"
                carcassHasFungi = true;
            }

            // ÉN pack pr. linje
            Pack wolfPack = null;
            Herd deerHerd = null;

            if (type.equals("wolf")) {
                wolfPack = new Pack();
            }
            if (type.equals("deer")) {
                deerHerd = new Herd();
            }

            // ─────────────────────────────────────
            // 3) SPAWN count OBJEKTER
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
                        Deer deer = new Deer(deerHerd);
                        world.setTile(l, deer);

                        // første hjort = leader → home
                        if (i == 0) deerHerd.setHome(l);
                    }

                    default -> {
                        // ukendt type i inputfilen → ignorer eller print
                        System.out.println("Ukendt type: " + type);
                    }
                }
            }
        }

        //int size = 5;
            //Program p = new Program(size, 800, 75);

            //World w = p.getWorld();

            // w.setTile(new Location(0, 0), new <MyClass>());

            // p.setDisplayInformation(<MyClass>.class, new DisplayInformation(<Color>, "<ImageName>"));

            //p.show();


        scanner.close();

// Start simulationen (GUI)
        program.show();
            for (int i = 0; i < 200; i++) {
            program.simulate();
            }
    }
}
