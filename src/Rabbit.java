import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Rabbit implements Actor {
    private int age;
    private int lifeEnergy;
    private int foodEnergy;
    private Burrow burrow;
    private boolean isAlive;
    private int AmountOfKids;
    private Random random;

    public Rabbit() {
        age = 0;
        AmountOfKids = 0;
        isAlive = true;
        lifeEnergy = 180;
        foodEnergy = 100;
    }

    @Override
    public void act(World world) {
        Location rabbitLocation = world.getLocation(this);
        Set<Location> emptyTilesNearRabbit = world.getEmptySurroundingTiles(rabbitLocation);
        List<Location> listOfPlacesToMove = new ArrayList<>(emptyTilesNearRabbit);

        random = new Random();

        if (!emptyTilesNearRabbit.isEmpty()) {
            int j = random.nextInt(emptyTilesNearRabbit.size());
            Location RabbitLocationToMoveTo = listOfPlacesToMove.get(j);
            world.move(this,RabbitLocationToMoveTo);
            if (world.containsNonBlocking(RabbitLocationToMoveTo)) {
                Object object = world.getNonBlocking(RabbitLocationToMoveTo);
                if (object instanceof Grass) {
                    world.delete(object);
                }

            }
        }
    }
}
