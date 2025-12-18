import Actors.*;
import Inanimate.*;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * The main class that sets up and runs the simulation.
 */
public class Main {
    /**
     * Main entry point for the simulation.
     * Reads input from a file, initializes the world, and starts the simulation.
     *
     * @param args Command line arguments (not used).
     * @throws FileNotFoundException If the input file is not found.
     */
    public static void main(String[] args) throws FileNotFoundException {

        InputStream is = Main.class.getClassLoader()
                .getResourceAsStream("input_files/tf4-MAX-test.txt");
        if (is == null) throw new FileNotFoundException("Inputfil ikke fundet");

        Scanner sc = new Scanner(is);
        Random rnd = new Random();

        int size = sc.nextInt();
        sc.nextLine();

        Program program = new Program(size, 800, 1000);
        World world = program.getWorld();

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String type = parts[0];

            int count = findCount(parts, rnd);

            boolean fungiFlag = contains(parts, "fungi");

            Location bearCenter = findCoords(parts);

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

    /**
     * Checks if a string array contains a specific word (case-insensitive).
     *
     * @param parts The string array to search in.
     * @param word The word to search for.
     * @return true if the word is found, false otherwise.
     */
    static boolean contains(String[] parts, String word) {
        for (String p : parts) if (p.equalsIgnoreCase(word)) return true;
        return false;
    }

    /**
     * Finds the first token in a string array that represents a count (e.g., "7" or "5-10").
     *
     * @param parts The string array to search.
     * @param rnd A Random object for generating counts from ranges.
     * @return The parsed count, or 1 if no count is found.
     */
    static int findCount(String[] parts, Random rnd) {
        for (String p : parts) {
            Integer c = parseCount(p, rnd);
            if (c != null) return c;
        }
        return 1;
    }

    /**
     * Parses a string token into an integer count. Supports single integers and ranges (e.g., "5-10").
     *
     * @param token The string token to parse.
     * @param rnd   A Random object for generating counts from ranges.
     * @return The parsed count, or null if the token is not a valid count.
     */
    static Integer parseCount(String token, Random rnd) {
        if (isDigits(token)) return Integer.parseInt(token);

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

    /**
     * Checks if a string consists only of digits.
     *
     * @param s The string to check.
     * @return true if the string is non-empty and contains only digits, false otherwise.
     */
    static boolean isDigits(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++)
            if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    /**
     * Finds the first token in a string array that represents coordinates (e.g., "(x,y)").
     *
     * @param parts The string array to search.
     * @return The parsed Location, or null if no coordinate token is found.
     */
    static Location findCoords(String[] parts) {
        for (String t : parts) {
            Location loc = parseCoords(t);
            if (loc != null) return loc;
        }
        return null;
    }

    /**
     * Parses a string token into a Location object. Supports coordinates in the format "(x,y)".
     *
     * @param token The string token to parse.
     * @return The parsed Location, or null if the token is not a valid coordinate format.
     */
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

    /**
     * Finds a random free location in the world.
     *
     * @param world The world to search in.
     * @param rnd   A Random object for generating locations.
     * @param size  The size of the world.
     * @param type  The type of object being placed (used to determine if it can overlap with other objects).
     * @return A random free Location.
     */
    static Location randomFreeLocation(World world, Random rnd, int size, String type) {
        boolean isBlockingType = type.equals("grass") || type.equals("fungi") || type.equals("bush");
        Location l;
        do {
            l = new Location(rnd.nextInt(size), rnd.nextInt(size));
        } while (isBlockingType ? world.containsNonBlocking(l) : !world.isTileEmpty(l));
        return l;
    }
}
