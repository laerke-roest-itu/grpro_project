package Inanimate;

import Actors.Wolf;

public class Pack extends Group<Wolf> {
    private Den den;

    public void claimDen(Den den) {
        this.den = den;
        for (Wolf w : getMembers()) {
            w.setDen(den);
        }
    }

    public Den getDen() {
        return den;
    }

}

