package me.redned.simcraft.city.world.network.piece;

import me.redned.levelparser.BlockState;
import me.redned.simcraft.city.network.NetworkData;
import me.redned.simcraft.city.schematic.CitySchematics;
import me.redned.simcraft.city.world.network.CityNetworkBuilder;
import me.redned.simcraft.schematic.Schematic;
import org.cloudburstmc.math.vector.Vector3i;

import java.util.concurrent.atomic.AtomicReference;

public class RailNetworkPiece implements NetworkPiece {
    private static final int SIZE = 16;

    private static final Schematic RAIL_SCHEMATIC = CitySchematics.getSchematic("rail");
    private static final BlockState GROUND_STATE = BlockState.of("minecraft:dirt");

    @Override
    public void buildPiece(CityNetworkBuilder builder, NetworkData network, Vector3i position) {
        AtomicReference<Vector3i> pastePosition = new AtomicReference<>();
        builder.pasteNetworkSchematic(RAIL_SCHEMATIC, network, pos -> {
            pos = pos.sub(0, 1, 0);
            pastePosition.set(pos);
            return pos;
        });

        // Fill terrain below rail
        for (int depth = -DEPTH; depth < 0; depth++) {
            int y = pastePosition.get().getY() + depth;
            for (int x = 0; x < SIZE; x++) {
                for (int z = 0; z < SIZE; z++) {
                    builder.getRegion().setBlockState(position.getX() + x, y, position.getZ() + z, GROUND_STATE);
                }
            }
        }
    }
}
