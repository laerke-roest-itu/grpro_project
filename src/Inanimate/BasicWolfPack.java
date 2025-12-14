package Inanimate;

import Actors.Wolf;

public class BasicWolfPack extends BasicPack<Wolf> implements WolfPack {
    private Den den;

    @Override
    public void claimDen(Den den) {
        this.den = den;
        for (Wolf w : getMembers()) {
            w.setDen(den);
        }
    }

    @Override
    public Den getDen() {
        return den;
    }

    @Override
    public void addMember(Wolf wolf) {
        super.addMember(wolf);
        wolf.setPack(this);
    }

}

