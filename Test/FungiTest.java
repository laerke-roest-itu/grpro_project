import Actors.Carcass;
import Inanimate.Fungi;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FungiTest {

    // Test for om svampen inficerer nærliggende ådsler
    @Test

    void testFungiInfectingNearbyCarcass() {
        World world = new World(10); // 10x10 f.eks.

        // opret locations
        Location fungiLoc = new Location(5,5);
        Location carcassLoc = new Location(6,5); // indenfor radius 1 af fungi

        // opret objekter (carcass uden automatisk spawn)
        Carcass carcass = new Carcass(100, 10); // meatLeft, rotTimer
        Fungi fungi = new Fungi(10); // kort levetid for test

        // placer i verden
        world.setTile(fungiLoc, fungi);
        world.setTile(carcassLoc, carcass);

        // valgfrit: hvis din Fungi.act itererer over surrounding tiles og kalder infectNearbyCarcass,
        // så kan vi kalde act direkte:
        fungi.act(world);

        // efter én tick burde carcass have fået fungi (hvis indenfor radius 2)
        assertNotNull(carcass.getFungi(), "Carcass should have been infected by nearby fungi");
        // jeg skal placere nogle carcass og en svamp i verdenen, og så køre et act og se om carcassene bliver inficeret
    } //problem 12.12.2025 - ser ud til den giver mig værdien itumulator.executable.DisplayInformation@4f638935

    // Test for om svampen dør efter sin levetid
    @Test
    void testFungiLifespan() {
        World world = new World(10);
        Location loc = new Location(2,2);
        Fungi fungi = new Fungi(2); // levetid = 2 ticks
        world.setTile(loc, fungi);

        // tick 1
        fungi.act(world);
        assertTrue(fungi.getLifespan() <= 1, "lifespan burde være blevet nedsænket");

        // tick 2
        fungi.act(world);
        // efter anden tick bør svampen være døende/slettet i act, afhængigt af implementering:
        Object tileAfter = world.getTile(loc);
        assertFalse(tileAfter instanceof Fungi, "Fungi burde være død og fjernet fra verdenen efter lifespan");
    } // 12.12.2025 - ser ud til den her test kører helt som forventet.

    @Test
    void testFungiHiddenInCarcass() {
        World world = new World(6);

        Location loc = new Location(3, 3);
        Carcass carcass = new Carcass(80, 2);
        Fungi fungi = new Fungi(10);

        // sæt fungi inde i carcass (ikke synlig på map)
        carcass.setFungi(fungi);

        // placer carcass på kortet (ikke fungi direkte)
        world.setTile(loc, carcass);

        // getInformation() på fungi skulle returnere null når den er inde i carcass
        // men vi skal først sikre at fungi.getFungiLocation(world) peger rigtigt.
        // Kald fungi.getInformation men sørg for at fungi's world-lokalisation svarer
        // Hvis du ikke kan få fungi.getInformation() uden at have fungi på world,
        // så juster test: kald carcass.getFungi().getInformation() og assert null
        assertNull(carcass.getFungi().getInformation(), "Fungi gemt i carcass bør ikke blive vist (getInformation == null)");
    }


}
