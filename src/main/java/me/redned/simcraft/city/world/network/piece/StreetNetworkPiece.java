package me.redned.simcraft.city.world.network.piece;

import me.redned.levelparser.BlockState;
import me.redned.simcraft.city.lot.LotData;
import me.redned.simcraft.city.network.NetworkData;
import me.redned.simcraft.city.world.CityRegion;
import me.redned.simcraft.city.world.network.CityNetworkBuilder;
import me.redned.simcraft.util.collection.RandomizedList;
import me.redned.simcraft.util.collection.TwoDimensionalPositionMap;
import me.redned.simreader.sc4.type.LotZoneWealth;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class StreetNetworkPiece implements NetworkPiece {
    private static final RandomizedList<BlockState> OUTER_DIRT_STATES = new RandomizedList<>(
            BlockState.of("minecraft:dirt"),
            BlockState.of("minecraft:coarse_dirt"),
            BlockState.of("minecraft:rooted_dirt")
    );

    private static final BlockState SIDEWALK_STATE = BlockState.of("minecraft:smooth_stone");
    private static final BlockState GRASS_STATE = BlockState.of("minecraft:grass_block");

    private static final RandomizedList<BlockState> PAVEMENT_STATES = new RandomizedList<>(
            BlockState.of("minecraft:stone"),
            BlockState.of("minecraft:gravel"),
            BlockState.of("minecraft:andesite")
    );

    private static final EnumMap<StreetDecoration, StreetDecorationBuilder> DECORATION_BUILDERS = new EnumMap<>(StreetDecoration.class) {
        {
            put(StreetDecoration.NONE, (region, network, x, y, z) -> { });
            put(StreetDecoration.DIRT, (region, network, x, y, z) -> region.setBlockState(x, y, z, OUTER_DIRT_STATES.iterator().next()));
            put(StreetDecoration.SIDEWALK, (region, network, x, y, z) -> region.setBlockState(x, y, z, SIDEWALK_STATE));
            put(StreetDecoration.GRASS_SIDEWALK, (region, network, x, y, z) -> {
                region.setBlockState(x, y, z, GRASS_STATE);

                if ((network.hasWestConnectivity() || network.hasEastConnectivity()) && ((z & 15) == 0 || (z & 15) == 1 || (z & 15) == 14 || (z & 15) == 15)) {
                    region.setBlockState(x, y, z, SIDEWALK_STATE);
                }

                if ((network.hasSouthConnectivity() || network.hasNorthConnectivity()) && ((x & 15) == 0 || (x & 15) == 1 || (x & 15) == 14 || (x & 15) == 15)) {
                    region.setBlockState(x, y, z, SIDEWALK_STATE);
                }

                // Fix corners
                if (network.hasNorthConnectivity()) {
                    if (((x & 15) == 2 || (x & 15) == 3 || (x & 15) == 12 || (x & 15) == 13) && ((z & 15) == 0 || (z & 15) == 1)) {
                        region.setBlockState(x, y, z, GRASS_STATE);
                    }
                }

                if (network.hasEastConnectivity()) {
                    if (((z & 15) == 2 || (z & 15) == 3 || (z & 15) == 12 || (z & 15) == 13) && ((x & 15) == 14 || (x & 15) == 15)) {
                        region.setBlockState(x, y, z, GRASS_STATE);
                    }
                }

                if (network.hasSouthConnectivity()) {
                    if (((x & 15) == 2 || (x & 15) == 3 || (x & 15) == 12 || (x & 15) == 13) && ((z & 15) == 14 || (z & 15) == 15)) {
                        region.setBlockState(x, y, z, GRASS_STATE);
                    }
                }

                if (network.hasWestConnectivity()) {
                    if (((z & 15) == 2 || (z & 15) == 3 || (z & 15) == 12 || (z & 15) == 13) && ((x & 15) == 0 || (x & 15) == 1)) {
                        region.setBlockState(x, y, z, GRASS_STATE);
                    }
                }
            });
        }
    };

    private static final int SIZE = 16;
    private static final int OUTER_EDGE_SIZE = 4;

    @Override
    public void buildPiece(CityNetworkBuilder builder, NetworkData network, Vector3i position) {
        CityRegion region = builder.getRegion();
        int tileX = position.getX() >> 4;
        int tileZ = position.getZ() >> 4;

        List<LotData> lots = getNearbyLots(region, tileX, tileZ);
        StreetDecoration decoration = getStreetDecoration(lots);
        StreetDecorationBuilder decorationBuilder = DECORATION_BUILDERS.get(decoration);

        int y = (network.getMinPosition().getFloorY() / builder.getTerrainGenerator().getHeightDivisor());

        for (int depth = -DEPTH; depth < 0; depth++) {
            TwoDimensionalPositionMap<Integer> yStorage = new TwoDimensionalPositionMap<>();

            // Build the center pavement (exists for all directions)
            for (int x = position.getX() + OUTER_EDGE_SIZE; x < position.getX() + (SIZE - OUTER_EDGE_SIZE); x++) {
                for (int z = position.getZ() + OUTER_EDGE_SIZE; z < position.getZ() + (SIZE - OUTER_EDGE_SIZE); z++) {
                    region.setBlockState(x, y + depth, z, PAVEMENT_STATES.iterator().next());

                    yStorage.put(x, z, y + depth);
                }
            }

            // Build the north part of the street
            {
                NetworkData connection = builder.getGroundNetwork(tileX, tileZ - 1);
                double height = getRaisedHeight(builder, position, connection);
                for (int x = position.getX(); x < position.getX() + SIZE; x++) {
                    for (int z = position.getZ(); z < position.getZ() + OUTER_EDGE_SIZE; z++) {
                        int index = z - position.getZ();
                        int yOffset = GenericMath.floor(height - (((index + 1.0D) / OUTER_EDGE_SIZE) * height));
                        int yPos = y + yOffset + depth;

                        if (network.hasNorthConnectivity() && (x >= position.getX() + OUTER_EDGE_SIZE && x < position.getX() + (SIZE - OUTER_EDGE_SIZE))) {
                            region.setBlockState(x, yPos, z, PAVEMENT_STATES.iterator().next());
                            yStorage.put(x, z, yPos);
                        } else
                            buildStreetDecoration(network, region, decoration, decorationBuilder, yStorage, x, yPos, z);
                    }
                }
            }

            // Build the east part of the street
            {
                NetworkData connection = builder.getGroundNetwork(tileX + 1, tileZ);
                double height = getRaisedHeight(builder, position, connection);
                for (int x = position.getX() + (SIZE - OUTER_EDGE_SIZE); x < position.getX() + SIZE; x++) {
                    int index = x - (position.getX() + (SIZE - OUTER_EDGE_SIZE));
                    int yOffset = GenericMath.floor(((index + 1.0D) / OUTER_EDGE_SIZE) * height);
                    int yPos = y + yOffset + depth;

                    for (int z = position.getZ(); z < position.getZ() + SIZE; z++) {
                        if (network.hasEastConnectivity() && (z >= position.getZ() + OUTER_EDGE_SIZE && z < position.getZ() + (SIZE - OUTER_EDGE_SIZE))) {
                            region.setBlockState(x, yPos, z, PAVEMENT_STATES.iterator().next());
                            yStorage.put(x, z, yPos);
                        } else
                            buildStreetDecoration(network, region, decoration, decorationBuilder, yStorage, x, yPos, z);
                    }
                }
            }

            // Build the south part of the street
            {
                NetworkData connection = builder.getGroundNetwork(tileX, tileZ + 1);
                double height = getRaisedHeight(builder, position, connection);
                for (int x = position.getX(); x < position.getX() + SIZE; x++) {
                    for (int z = position.getZ() + (SIZE - OUTER_EDGE_SIZE); z < position.getZ() + SIZE; z++) {
                        int index = z - (position.getZ() + (SIZE - OUTER_EDGE_SIZE));
                        int yOffset = GenericMath.floor(((index + 1.0D) / OUTER_EDGE_SIZE) * height);
                        int yPos = y + yOffset + depth;

                        if (network.hasSouthConnectivity() && (x >= position.getX() + OUTER_EDGE_SIZE && x < position.getX() + (SIZE - OUTER_EDGE_SIZE))) {
                            region.setBlockState(x, yPos, z, PAVEMENT_STATES.iterator().next());
                            yStorage.put(x, z, yPos);
                        } else
                            buildStreetDecoration(network, region, decoration, decorationBuilder, yStorage, x, yPos, z);
                    }
                }
            }

            // Build the west part of the street
            {
                NetworkData connection = builder.getGroundNetwork(tileX - 1, tileZ);
                double height = getRaisedHeight(builder, position, connection);
                for (int x = position.getX(); x < position.getX() + OUTER_EDGE_SIZE; x++) {
                    int index = x - position.getX();
                    int yOffset = GenericMath.floor(height - (((index + 1.0D) / OUTER_EDGE_SIZE) * height));
                    int yPos = y + yOffset + depth;

                    for (int z = position.getZ(); z < position.getZ() + SIZE; z++) {
                        if (network.hasWestConnectivity() && (z >= position.getZ() + OUTER_EDGE_SIZE && z < position.getZ() + (SIZE - OUTER_EDGE_SIZE))) {
                            region.setBlockState(x, yPos, z, PAVEMENT_STATES.iterator().next());
                            yStorage.put(x, z, yPos);
                        } else {
                            buildStreetDecoration(network, region, decoration, decorationBuilder, yStorage, x, yPos, z);
                        }
                    }
                }
            }
        }
    }

    private static void buildStreetDecoration(NetworkData network, CityRegion region, StreetDecoration decoration, StreetDecorationBuilder decorationBuilder, TwoDimensionalPositionMap<Integer> yStorage, int x, int yPos, int z) {
        if (decoration == StreetDecoration.NONE) {
            return;
        }

        Integer previousHeight = yStorage.get(x, z);
        if (previousHeight != null) {
            if (previousHeight <= yPos) {
                decorationBuilder.build(region, network, x, previousHeight, z);
            } else {
                region.setBlockState(x, previousHeight, z, BlockState.AIR);
                decorationBuilder.build(region, network, x, yPos, z);

                yStorage.put(x, z, yPos);
            }
        } else {
            decorationBuilder.build(region, network, x, yPos, z);
            yStorage.put(x, z, yPos);
        }
    }

    private static double getRaisedHeight(CityNetworkBuilder builder, Vector3i position, NetworkData connection) {
        double height = 0;
        if (connection != null) {
            Vector3i connectionPos = connection.getMinPosition().div(1, builder.getTerrainGenerator().getHeightDivisor(), 1).add(0, 1, 0).toInt();
            height = connectionPos.getY() - position.getY();
        }

        height /= 2.0D; // Each side of the road accommodates for half the height
        return height;
    }

    private static List<LotData> getNearbyLots(CityRegion region, int tileX, int tileZ) {
        List<LotData> lots = new ArrayList<>();

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // Don't check on self
                if (x == 0 && z == 0) {
                    continue;
                }

                LotData lot = region.getLot(tileX + x, tileZ + z);
                if (lot != null) {
                    lots.add(lot);
                }
            }
        }

        return lots;
    }

    private static StreetDecoration getStreetDecoration(List<LotData> lots) {
        StreetDecoration decoration = StreetDecoration.NONE;

        int zoneWealthValue = 0;
        for (LotData lot : lots) {
            if (decoration == StreetDecoration.NONE) {
                if (lot.isIndustrial()) {
                    if (lot.getZoneWealth() == LotZoneWealth.$) {
                        decoration = StreetDecoration.DIRT;
                    } else {
                        decoration = StreetDecoration.GRASS_SIDEWALK;
                    }
                }
            }

            if (!lot.isIndustrial()) {
                decoration = StreetDecoration.GRASS_SIDEWALK;
            }

            if (!lot.isIndustrial() && (lot.getZoneWealth() == LotZoneWealth.$$) || lot.getZoneWealth() == LotZoneWealth.$$$) {
                zoneWealthValue++;
            }
        }

        if (zoneWealthValue >= 2) {
            return StreetDecoration.SIDEWALK;
        }
        
        return decoration == StreetDecoration.NONE ? StreetDecoration.DIRT : decoration; // TODO: Needs fixing, but dirt looks okay for now
    }

    private enum StreetDecoration {
        NONE,
        DIRT,
        GRASS_SIDEWALK,
        SIDEWALK
    }

    private interface StreetDecorationBuilder {
        void build(CityRegion region, NetworkData network, int x, int y, int z);
    }
}
