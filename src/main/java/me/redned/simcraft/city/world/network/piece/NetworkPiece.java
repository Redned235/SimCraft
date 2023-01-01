package me.redned.simcraft.city.world.network.piece;

import me.redned.simcraft.city.network.NetworkData;
import me.redned.simcraft.city.world.network.CityNetworkBuilder;
import org.cloudburstmc.math.vector.Vector3i;

public interface NetworkPiece {
    int DEPTH = 16;

    void buildPiece(CityNetworkBuilder region, NetworkData network, Vector3i position);
}
