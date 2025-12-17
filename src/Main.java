import Actors.*;
import Inanimate.*;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {

        InputStream is = Main.class.getClassLoader()
                .getResourceAsStream("input_files/_deer.txt");
        if (is == null) throw new FileNotFoundException("Inputfil ikke fundet");

        Scanner sc = new Scanner(is);
        Random rnd = new Random();

        int size = sc.nextInt();
        sc.nextLine(); // resten af linjen væk

        Program program = new Program(size, 800, 1000);
        World world = program.getWorld();

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String type = parts[0];

            // 1) find count (tal eller min-max). Hvis ingen: count = 1
            int count = findCount(parts, rnd);

            // 2) carcass-flag
            boolean fungiFlag = contains(parts, "fungi");

            // 3) bear coords (valgfri)
            Location bearCenter = findCoords(parts);

            // pack/herd pr. linje
            Pack wolfPack = type.equals("wolf") ? new Pack() : null;
            Herd deerHerd = type.equals("deer") ? new Herd() : null;

            for (int i = 0; i < count; i++) {
                Location l = randomFreeLocation(world, rnd, size, type);

                switch (type) {
                    case "grass" -> world.setTile(l, new Grass());
                    case "fungi" -> world.setTile(l, new Fungi(100));
                    case "bush"  -> world.setTile(l, new Bush());
                    case "burrow"-> world.setTile(l, new Burrow());

                    case "carcass" -> {
                        if (fungiFlag) world.setTile(l, new Carcass(80, 25, true));
                        else           world.setTile(l, new Carcass(80, 25));
                    }

                    case "rabbit" -> world.setTile(l, new Rabbit());

                    case "wolf" -> world.setTile(l, new Wolf(wolfPack));

                    case "deer" -> {
                        Deer deer = new Deer(deerHerd);
                        world.setTile(l, deer);
                        if (i == 0) deerHerd.setHome(l);
                    }

                    case "bear" -> {
                        Location center = (bearCenter != null)
                                ? bearCenter
                                : new Location(rnd.nextInt(size), rnd.nextInt(size));
                        world.setTile(l, new Bear(center));
                    }

                    default -> System.out.println("Ukendt type: " + type + " (linje: " + line + ")");
                }
            }
        }

        sc.close();

        program.show();
        for (int i = 0; i < 200; i++) program.simulate();
    }

    // --------- små helper-metoder (holdt simple) ---------

    static boolean contains(String[] parts, String word) {
        for (String p : parts) if (p.equalsIgnoreCase(word)) return true;
        return false;
    }

    // finder første token der er "7" eller "5-10"
    static int findCount(String[] parts, Random rnd) {
        for (String p : parts) {
            Integer c = parseCount(p, rnd);
            if (c != null) return c;
        }
        return 1; // default hvis der ikke står noget count
    }

    static Integer parseCount(String token, Random rnd) {
        // "12"
        if (isDigits(token)) return Integer.parseInt(token);

        // "5-10"
        int dash = token.indexOf('-');
        if (dash > 0) {
            String a = token.substring(0, dash);
            String b = token.substring(dash + 1);
            if (isDigits(a) && isDigits(b)) {
                int min = Integer.parseInt(a);
                int max = Integer.parseInt(b);
                if (max >= min) return min + rnd.nextInt(max - min + 1);
            }
        }
        return null;
    }

    static boolean isDigits(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++)
            if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    // finder token som "(x,y)" og parser det uden regex
    static Location findCoords(String[] parts) {
        for (String t : parts) {
            Location loc = parseCoords(t);
            if (loc != null) return loc;
        }
        return null;
    }

    static Location parseCoords(String token) {
        token = token.trim();
        if (!token.startsWith("(") || !token.endsWith(")")) return null;

        String inside = token.substring(1, token.length() - 1);
        int comma = inside.indexOf(',');
        if (comma < 0) return null;

        String xs = inside.substring(0, comma).trim();
        String ys = inside.substring(comma + 1).trim();

        if (!isDigits(xs) || !isDigits(ys)) return null;

        return new Location(Integer.parseInt(xs), Integer.parseInt(ys));
    }

    static Location randomFreeLocation(World world, Random rnd, int size, String type) {
        Location l;
        do {
            l = new Location(rnd.nextInt(size), rnd.nextInt(size));
        } while (
                (type.equals("grass") || type.equals("fungi") || type.equals("bush"))
                        ? world.containsNonBlocking(l)
                        : !world.isTileEmpty(l)
        );
        return l;
    }
}
