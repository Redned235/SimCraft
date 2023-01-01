package me.redned.simcraft.city.world.network.piece;

import me.redned.levelparser.BlockState;
import me.redned.simcraft.city.network.NetworkData;
import me.redned.simcraft.city.world.CityRegion;
import me.redned.simcraft.city.world.network.CityNetworkBuilder;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3i;

import java.util.HashMap;
import java.util.Map;

public class RoadNetworkPiece implements NetworkPiece {
    private static final BlockState SIDEWALK_STATE = BlockState.of("minecraft:smooth_stone");
    private static final BlockState PAVEMENT_STATE = BlockState.of("minecraft:coal_block");
    private static final BlockState PAINT_STATE = BlockState.of("minecraft:yellow_concrete");
    private static final BlockState CROSSWALK_STATE = BlockState.of("minecraft:smooth_quartz");

    private static final int SIZE = 16;
    private static final int OUTER_EDGE_SIZE = 3;

    private static final int[] CROSSWALK_SPACING = { 4, 6, 9, 11 };

    @Override
    public void buildPiece(CityNetworkBuilder builder, NetworkData network, Vector3i position) {
        CityRegion region = builder.getRegion();
        int tileX = position.getX() >> 4;
        int tileZ = position.getZ() >> 4;

        int y = (network.getMinPosition().getFloorY() / builder.getTerrainGenerator().getHeightDivisor());

        for (int depth = -DEPTH; depth < 0; depth++) {
            int connections = 0;

            boolean northConnectivity = false;
            boolean eastConnectivity = false;
            boolean southConnectivity = false;
            boolean westConnectivity = false;

            Map<Vector2i, Integer> yStorage = new HashMap<>();

            // Build the center pavement (exists for all directions)
            for (int x = position.getX() + OUTER_EDGE_SIZE; x < position.getX() + (SIZE - OUTER_EDGE_SIZE); x++) {
                for (int z = position.getZ() + OUTER_EDGE_SIZE; z < position.getZ() + (SIZE - OUTER_EDGE_SIZE); z++) {
                    region.setBlockState(x, y + depth, z, PAVEMENT_STATE);

                    yStorage.put(Vector2i.from(x, z), y + depth);
                }
            }

            // Build the north part of the road
            {
                NetworkData connection = builder.getGroundNetwork(tileX, tileZ - 1);
                double height = getRaisedHeight(builder, position, connection);
                for (int x = position.getX(); x < position.getX() + SIZE; x++) {
                    for (int z = position.getZ(); z < position.getZ() + OUTER_EDGE_SIZE; z++) {
                        int index = z - position.getZ();
                        int yOffset = GenericMath.floor(height - (((index + 1.0D) / OUTER_EDGE_SIZE) * height));
                        int yPos = y + yOffset + depth;

                        if ((northConnectivity = (network.hasNorthConnectivity() || (connection != null && connection.hasSouthConnectivity()))) && (x >= position.getX() + OUTER_EDGE_SIZE && x < position.getX() + (SIZE - OUTER_EDGE_SIZE))) {
                            region.setBlockState(x, yPos, z, PAVEMENT_STATE);
                            yStorage.put(Vector2i.from(x, z), yPos);
                        } else {
                            buildPavement(region, yStorage, x, yPos, z);
                        }
                    }
                }
            }

            // Build the east part of the road
            {
                NetworkData connection = builder.getGroundNetwork(tileX + 1, tileZ);
                double height = getRaisedHeight(builder, position, connection);
                for (int x = position.getX() + (SIZE - OUTER_EDGE_SIZE); x < position.getX() + SIZE; x++) {
                    int index = x - (position.getX() + (SIZE - OUTER_EDGE_SIZE));
                    int yOffset = GenericMath.floor(((index + 1.0D) / OUTER_EDGE_SIZE) * height);
                    int yPos = y + yOffset + depth;

                    for (int z = position.getZ(); z < position.getZ() + SIZE; z++) {
                        if ((eastConnectivity = (network.hasEastConnectivity() || (connection != null && connection.hasWestConnectivity()))) && (z >= position.getZ() + OUTER_EDGE_SIZE && z < position.getZ() + (SIZE - OUTER_EDGE_SIZE))) {
                            region.setBlockState(x, yPos, z, PAVEMENT_STATE);
                            yStorage.put(Vector2i.from(x, z), yPos);
                        } else {
                            buildPavement(region, yStorage, x, yPos, z);
                        }
                    }
                }
            }

            // Build the south part of the road
            {
                NetworkData connection = builder.getGroundNetwork(tileX, tileZ + 1);
                double height = getRaisedHeight(builder, position, connection);
                for (int x = position.getX(); x < position.getX() + SIZE; x++) {
                    for (int z = position.getZ() + (SIZE - OUTER_EDGE_SIZE); z < position.getZ() + SIZE; z++) {
                        int index = z - (position.getZ() + (SIZE - OUTER_EDGE_SIZE));
                        int yOffset = GenericMath.floor(((index + 1.0D) / OUTER_EDGE_SIZE) * height);
                        int yPos = y + yOffset + depth;

                        if ((southConnectivity = (network.hasSouthConnectivity() || (connection != null && connection.hasNorthConnectivity()))) && (x >= position.getX() + OUTER_EDGE_SIZE && x < position.getX() + (SIZE - OUTER_EDGE_SIZE))) {
                            region.setBlockState(x, yPos, z, PAVEMENT_STATE);
                            yStorage.put(Vector2i.from(x, z), yPos);
                        } else {
                            buildPavement(region, yStorage, x, yPos, z);
                        }
                    }
                }
            }

            // Build the west part of the road
            {
                NetworkData connection = builder.getGroundNetwork(tileX - 1, tileZ);
                double height = getRaisedHeight(builder, position, connection);
                for (int x = position.getX(); x < position.getX() + OUTER_EDGE_SIZE; x++) {
                    int index = x - position.getX();
                    int yOffset = GenericMath.floor(height - (((index + 1.0D) / OUTER_EDGE_SIZE) * height));
                    int yPos = y + yOffset + depth;

                    for (int z = position.getZ(); z < position.getZ() + SIZE; z++) {
                        if ((westConnectivity = (network.hasWestConnectivity() || (connection != null && connection.hasEastConnectivity()))) && (z >= position.getZ() + OUTER_EDGE_SIZE && z < position.getZ() + (SIZE - OUTER_EDGE_SIZE))) {
                            region.setBlockState(x, yPos, z, PAVEMENT_STATE);
                            yStorage.put(Vector2i.from(x, z), yPos);
                        } else {
                            buildPavement(region, yStorage, x, yPos, z);
                        }
                    }
                }
            }

            // Sync connection count
            if (northConnectivity) connections++;
            if (eastConnectivity) connections++;
            if (southConnectivity) connections++;
            if (westConnectivity) connections++;

            // Draw N/S lines
            if (northConnectivity && southConnectivity && !eastConnectivity && !westConnectivity) {
                for (int z = position.getZ(); z < position.getZ() + SIZE; z++) {
                    int line1 = position.getX() + 6;
                    int line2 = position.getX() + 9;

                    region.setBlockState(line1, yStorage.get(Vector2i.from(line1, z)), z, PAINT_STATE);
                    region.setBlockState(line2, yStorage.get(Vector2i.from(line2, z)), z, PAINT_STATE);
                }
            }

            // Draw E/W lines
            if (eastConnectivity && westConnectivity && !northConnectivity && !southConnectivity) {
                for (int x = position.getX(); x < position.getX() + SIZE; x++) {
                    int line1 = position.getZ() + 6;
                    int line2 = position.getZ() + 9;

                    region.setBlockState(x, yStorage.get(Vector2i.from(x, line1)), line1, PAINT_STATE);
                    region.setBlockState(x, yStorage.get(Vector2i.from(x, line2)), line2, PAINT_STATE);
                }
            }

            // If we have 4 connections, the road is
            // an intersection, and we need to draw crosswalks
            if (connections >= 4) {
                int z = position.getZ() + 1;
                for (int offset : CROSSWALK_SPACING) {
                    int xPos = position.getX() + offset;

                    region.setBlockState(xPos, yStorage.get(Vector2i.from(xPos, z)), z, CROSSWALK_STATE);
                    region.setBlockState(xPos, yStorage.get(Vector2i.from(xPos, z + 1)), z + 1, CROSSWALK_STATE);

                    region.setBlockState(xPos, yStorage.get(Vector2i.from(xPos, z + 12)), z + 12, CROSSWALK_STATE);
                    region.setBlockState(xPos, yStorage.get(Vector2i.from(xPos, z + 13)), z + 13, CROSSWALK_STATE);
                }

                int x = position.getX() + 1;
                for (int offset : CROSSWALK_SPACING) {
                    int zPos = position.getZ() + offset;

                    region.setBlockState(x, yStorage.get(Vector2i.from(x, zPos)), zPos, CROSSWALK_STATE);
                    region.setBlockState(x + 1, yStorage.get(Vector2i.from(x + 1, zPos)), zPos, CROSSWALK_STATE);

                    region.setBlockState(x + 12, yStorage.get(Vector2i.from(x + 12, zPos)), zPos, CROSSWALK_STATE);
                    region.setBlockState(x + 13, yStorage.get(Vector2i.from(x + 13, zPos)), zPos, CROSSWALK_STATE);
                }
            }
        }
    }

    private static void buildPavement(CityRegion region, Map<Vector2i, Integer> yStorage, int x, int yPos, int z) {
        Integer previousHeight = yStorage.get(Vector2i.from(x, z));
        if (previousHeight != null) {
            if (previousHeight <= yPos) {
                region.setBlockState(x, previousHeight, z, SIDEWALK_STATE);
            } else {
                region.setBlockState(x, previousHeight, z, BlockState.AIR);
                region.setBlockState(x, yPos, z, SIDEWALK_STATE);

                yStorage.put(Vector2i.from(x, z), yPos);
            }
        } else {
            region.setBlockState(x, yPos, z, SIDEWALK_STATE);
            yStorage.put(Vector2i.from(x, z), yPos);
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
}
