import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;

import itumulator.executable.DisplayInformation;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        InputStream is = Main.class
                .getClassLoader()
                .getResourceAsStream("input_files/tf1-1.txt");

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

        // Variabel til "count" = hvor mange objekter af en given type der skal laves
        int count = 0;

        // Opret ITUmulator-programmet med den læste størrelse
        Program program = new Program(size, display_size, delay);
        // Hent verden (World) ud af programmet – det er her vi placerer objekter
        World world = program.getWorld();


        // Læs resten af filen linje for linje (egentlig token for token)
        while (scanner.hasNext()) {
            // type: fx "grass", "person" osv.
            String type = scanner.next();
            // amount: enten "10" eller "3-7"
            String amount = scanner.next();
            //System.out.println("Læser type = " + type + ", amount = " + amount);
            // Tjek om amount er et interval (min-max) eller et enkelt tal
            if (amount.contains("-")) {
                // Del teksten op ved "-" → "3-7" bliver ["3", "7"]
                String[] parts = amount.split("-");
                int min = Integer.parseInt(parts[0]); // konverter "3" til 3
                int max = Integer.parseInt(parts[1]); // konverter "7" til 7

                // Vælg et tilfældigt antal mellem min og max (begge inkl.)
                // random.nextInt(max - min + 1) giver et tal i [0, max-min]
                // + min flytter intervallet op til [min, max]
                count = min + random.nextInt(max - min + 1);
            } else {
                // Hvis der ikke står "-", er det bare et præcist antal
                count = Integer.parseInt(amount);
            }

            // Placér 'count' objekter af den pågældende type tilfældigt i verden
            for (int i = 0; i < count; i++) {
                // Vælg en tilfældig (x,y)-position i verden
                int x = random.nextInt(size);
                int y = random.nextInt(size);
                Location l = new Location(x, y);
                //System.out.println("Placerer " + type + " på (" + x + "," + y + ")");
                // Hvis typen fra filen var "grass", placerer vi Grass på feltet
                // (andre typer kan også placeres)
                if (type.equals("grass")) {
                    // Så længe der ALLEREDE står et non-blocking objekt på feltet,
                    // vælg en ny tilfældig position (vi vil undgå at placere Grass
                    // ovenpå andet non-blocking, fx andet græs)
                    while (world.containsNonBlocking(l)) {
                        x = random.nextInt(size);
                        y = random.nextInt(size);
                        l = new Location(x, y);
                    }


                    world.setTile(l, new Grass());
                } else if (type.equals("burrow")) {
                    while (world.containsNonBlocking(l)) {
                        x = random.nextInt(size);
                        y = random.nextInt(size);
                        l = new Location(x, y);
                    }
                    world.setTile(l, new Burrow());
                } else if (type.equals("rabbit")) {
                    // Så længe der ALLEREDE står et objekt på feltet,
                    // vælg en ny tilfældig position (vi vil undgå at placere Rabbit
                    // ovenpå andet objekt)
                    while (!world.isTileEmpty(l)) {
                        x = random.nextInt(size);
                        y = random.nextInt(size);
                        l = new Location(x, y);
                    }
                    world.setTile(l, new Rabbit());
                }


            }
        }
        scanner.close();

        // ===== VISNING / ANIMATION =====

// Fortæl ITUmulator hvordan objekterne skal tegnes
        program.setDisplayInformation(Grass.class,
                new DisplayInformation(Color.GREEN, "grass"));

        program.setDisplayInformation(Rabbit.class,
                new DisplayInformation(Color.WHITE, "rabbit-small"));

        program.setDisplayInformation(Burrow.class,
                new DisplayInformation(Color.ORANGE, "hole-small"));


// Start simulationen (GUI)
        program.show();
        for (int i = 0; i < 200; i++) {
            program.simulate();
        }


        //int size = 5;
        //Program p = new Program(size, 800, 75);

        //World w = p.getWorld();

        // w.setTile(new Location(0, 0), new <MyClass>());

        // p.setDisplayInformation(<MyClass>.class, new DisplayInformation(<Color>, "<ImageName>"));

        //p.show();
    }
}
