package Inanimate;

import Actors.Deer;

public class BasicDeerPack extends BasicPack<Deer> implements DeerPack {

    @Override
    public void addMember(Deer deer) {
        super.addMember(deer);
        deer.setPack(this); // holder relationen konsistent (ligesom vi gjorde med ulve)
    }
}

