package Inanimate;

import Actors.Wolf;

public interface WolfPack extends Pack<Wolf> {
    void claimDen(Den den);
    Den getDen();
}

