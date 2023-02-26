package me.redned.simcraft.city.world.network;

import lombok.Getter;
import me.redned.levelparser.BlockState;
import me.redned.levelparser.Chunk;
import me.redned.simcraft.city.network.NetworkData;
import me.redned.simcraft.city.network.NetworkTile1Data;
import me.redned.simcraft.city.world.CityRegion;
import me.redned.simcraft.city.world.network.piece.NetworkPiece;
import me.redned.simcraft.city.world.network.piece.RoadNetworkPiece;
import me.redned.simcraft.city.world.network.piece.StreetNetworkPiece;
import me.redned.simcraft.city.world.terrain.CityTerrainGenerator;
import me.redned.simcraft.util.collection.TwoDimensionalPositionMap;
import me.redned.simreader.sc4.type.network.NetworkType;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
public class CityNetworkBuilder {
    private static final Map<NetworkType, NetworkPiece> NETWORK_PIECES = new EnumMap<>(NetworkType.class) {
        {
            put(NetworkType.STREET, new StreetNetworkPiece());
            put(NetworkType.ROAD, new RoadNetworkPiece());
        }
    };

    private final CityRegion region;
    private final CityTerrainGenerator terrainGenerator;

    private final List<NetworkData> networks;

    private final TwoDimensionalPositionMap<NetworkData> groundLevelNetwork = new TwoDimensionalPositionMap<>();

    public CityNetworkBuilder(CityRegion region, CityTerrainGenerator terrainGenerator) {
        this.region = region;
        this.terrainGenerator = terrainGenerator;

        this.networks = new ArrayList<>();
        this.networks.addAll(region.getCity().getNetworkTile1s());
        this.networks.addAll(region.getCity().getNetworkTile2s());

        // Build our ground level network
        for (NetworkTile1Data tile : this.region.getCity().getNetworkTile1s()) {
            Vector3i pos = tile.getMinPosition().toInt();
            this.groundLevelNetwork.put(pos.getX() >> 4, pos.getZ() >> 4, tile);
        }
    }

    public void buildNetworks() {
        int heightDivisor = this.terrainGenerator.getHeightDivisor();
        for (NetworkData network : this.networks) {
            Vector3i minPos = network.getMinPosition().div(1, heightDivisor, 1).add(0, 1, 0).toInt();

            NetworkPiece piece = NETWORK_PIECES.get(network.getNetworkType());
            if (piece != null) {
                piece.buildPiece(this, network, minPos);
            }

            if (this.region.isDebug()) {
                Vector3i maxPos = network.getMaxPosition().div(1, heightDivisor, 1).add(0, 1, 0).toInt();
                Chunk chunk = this.region.getChunk(minPos.getX() >> 4, minPos.getZ() >> 4);
                chunk.getBlockEntities().add(NbtMap.builder()
                        .putString("id", "minecraft:sign")
                        .putInt("x", minPos.getX() + this.region.getMinPosition().getX())
                        .putInt("y", maxPos.getY())
                        .putInt("z", minPos.getZ() + this.region.getMinPosition().getY())
                        .putString("Text1", "{\"text\":\"" + network.getNetworkType() + "\"}")
                        .putString("Text2", "{\"text\":\"" + network.getOrientation() + " (" + network.getRotation() + ")" + "\"}")
                        .putString("Text3", "{\"text\":\"" + (network.hasEastConnectivity() || network.hasWestConnectivity()) + "\"}")
                        .build());

                this.region.setBlockState(minPos.getX(), maxPos.getY(), minPos.getZ(), BlockState.of("minecraft:oak_sign"));
            }
        }
    }

    public NetworkData getGroundNetwork(int tileX, int tileZ) {
        return this.groundLevelNetwork.get(tileX, tileZ);
    }
}
