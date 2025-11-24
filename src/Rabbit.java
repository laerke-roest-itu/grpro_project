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
    private final Random random;

    public Rabbit() {
        age = 0;
        AmountOfKids = 0;
        isAlive = true;
        lifeEnergy = 180;
        foodEnergy = 100;
        random = new Random();
    }

    @Override
    public void act(World world) {
        age++;
        lifeEnergy = lifeEnergy - 1;

        if (lifeEnergy <= 0 || foodEnergy <= 0) {
            world.delete(this);
            return;
        }

        Location rabbitLocation = world.getLocation(this);
        Set<Location> emptyTilesNearRabbit = world.getEmptySurroundingTiles(rabbitLocation);
        List<Location> listOfPlacesToMove = new ArrayList<>(emptyTilesNearRabbit);

        if (!emptyTilesNearRabbit.isEmpty()) {
            int j = random.nextInt(emptyTilesNearRabbit.size());
            Location RabbitLocationToMoveTo = listOfPlacesToMove.get(j);
            world.move(this,RabbitLocationToMoveTo);
            foodEnergy = foodEnergy - 5;
            if (world.containsNonBlocking(RabbitLocationToMoveTo)) {
                Object object = world.getNonBlocking(RabbitLocationToMoveTo);
                if (object instanceof Grass && foodEnergy < 50) {
                    world.delete(object);
                    foodEnergy = foodEnergy + 10;
                }

            }
        }
    }

}
