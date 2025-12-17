package Inanimate;

import Actors.Wolf;

/**
 * A class representing a pack of wolves.
 */
public class Pack extends Group<Wolf> {
    private Den den;

    /**
     * Claims a den for the pack and assigns it to all members.
     *
     * @param den the den to claim
     */
    public void claimDen(Den den) {
        this.den = den;
        for (Wolf w : getMembers()) {
            w.setDen(den);
        }
    }

    /**
     * Gets the den of the pack.
     *
     * @return the den
     */
    public Den getDen() {
        return den;
    }

}

