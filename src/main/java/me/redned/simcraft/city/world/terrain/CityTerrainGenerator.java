package me.redned.simcraft.city.world.terrain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.redned.levelparser.BlockState;
import me.redned.simcraft.city.City;
import me.redned.simcraft.city.network.NetworkData;
import me.redned.simcraft.city.placeable.BuildingData;
import me.redned.simcraft.city.schematic.CitySchematics;
import me.redned.simcraft.city.world.CityRegion;
import me.redned.simcraft.city.world.network.piece.NetworkPiece;
import me.redned.simcraft.schematic.Schematic;
import me.redned.simcraft.util.Pair;
import me.redned.simcraft.util.collection.TwoDimensionalPositionMap;
import me.redned.simcraft.util.heightmap.HeightMap;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class CityTerrainGenerator {
    private static final int MAX_HEIGHT = 1024;
    private static final int SMOOTHING_PASSES = 50;

    private static final int WATER_Y_LEVEL = 250;
    private static final int SAND_WATER_HEIGHT_LEVEL = 5;
    private static final int STONE_CHANCE = 5;
    private static final int DIRT_DEPTH = 1;
    private static final int STONE_DEPTH = 64;

    private static final BlockState GRASS = BlockState.of("minecraft:grass_block");
    private static final BlockState DIRT = BlockState.of("minecraft:dirt");
    private static final BlockState SAND = BlockState.of("minecraft:sand");
    private static final BlockState STONE = BlockState.of("minecraft:dripstone_block");
    private static final BlockState WATER = BlockState.of("minecraft:water");

    private final CityRegion region;

    @Getter
    private final int heightDivisor;

    @Getter
    private HeightMap heightMap;

    private final TwoDimensionalPositionMap<Pair<Schematic, Integer>> tileSchematics = new TwoDimensionalPositionMap<>();

    public void buildTerrain() {
        City city = this.region.getCity();
        float[][] rawHeightMap = city.getHeightMap().clone();

        // Sync our lot heights
        this.region.getLots().forEach((x, y, lot) -> rawHeightMap[y][x] = lot.getYPosition());

        // Go through our buildings and see if there are any that
        // will be placed inside the terrain (i.e. agriculture plots)
        for (BuildingData building : city.getBuildings()) {
            Schematic schematic = CitySchematics.getSchematic(building.getIdentifier());
            if (schematic != null) {
                if (schematic.getMetadata().getBoolean("SCTerrainBlend", false)) {
                    // Blend the chunk data with the terrain
                    this.tileSchematics.put((int) building.getMinPosition().getX() >> 4, (int) building.getMinPosition().getZ() >> 4, new Pair<>(schematic, building.getRotation()));
                }
            }
        }

        HeightMap heightMap = new HeightMap(rawHeightMap, city.getDimensions().getX(), city.getDimensions().getY(), SMOOTHING_PASSES);

        // Set our city heightmap
        this.heightMap = heightMap;

        // The swap of X & Z is intentional, despite X being
        // first in the array. SimCity considers its X tile to what
        // Minecraft uses as Z, and it's Y tile as what Minecraft
        // uses as X.
        for (int chunkZ = 1; chunkZ < rawHeightMap.length; chunkZ++) {
            float[] zPositions = rawHeightMap[chunkZ];
            for (int chunkX = 1; chunkX < zPositions.length; chunkX++) {
                // Check if we have a schematic occupying this chunk. This allows the terrain generator
                // to use blocks from the schematic rather than the default ones. Main purpose for this is to
                // allow for certain tiles (i.e. agriculture tiles) to blend in with the terrain, so they don't look
                // like terraces.
                List<Vector3i> occupiedPositions = new ArrayList<>();
                Pair<Schematic, Integer> occupyingSchematic = this.tileSchematics.get(chunkX - 1, chunkZ - 1);
                if (occupyingSchematic != null) {
                    occupyingSchematic.key().paste(this.region.getLevel().getLevel(), Vector3i.from((chunkX - 1) << 4, 0, (chunkZ - 1) << 4), occupyingSchematic.value(), (initialPos, schemPos) -> {
                        int blockX = schemPos.getX();
                        int blockZ = schemPos.getZ();

                        float height = heightMap.getData()[Math.max(0, blockZ - 1)][Math.max(0, blockX - 1)] / this.heightDivisor;
                        Vector3i pos = fixOccupiedChunkOffset(Vector3i.from(blockX, height + initialPos.getY(), blockZ), occupyingSchematic.value());
                        occupiedPositions.add(pos);
                        return pos.add(this.region.getMinPosition().getX(), 0, this.region.getMinPosition().getY()); // Add minimum positions
                    }, false);
                }

                NetworkData groundNetwork = this.region.getNetworkBuilder().getGroundNetwork((chunkX - 1), (chunkZ - 1));

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int blockX = ((chunkX - 1) << 4) + x;
                        int blockZ = ((chunkZ - 1) << 4) + z;
                        float height = heightMap.getData()[blockZ][blockX];

                        // Use ground network height if we have one
                        if (groundNetwork != null) {
                            height = groundNetwork.getMinPosition().getY() - NetworkPiece.DEPTH;
                        }

                        // Generate water if the terrain is below the water Y
                        int blockHeight = GenericMath.floor(height / this.heightDivisor);

                        boolean generateWater = blockHeight < (WATER_Y_LEVEL / this.heightDivisor);
                        if (generateWater) {
                            this.region.setBlockState(blockX, (WATER_Y_LEVEL / this.heightDivisor), blockZ, WATER);
                        }

                        if (generateWater || (blockHeight - (SAND_WATER_HEIGHT_LEVEL / this.heightDivisor)) < (WATER_Y_LEVEL / this.heightDivisor)) {
                            this.region.setBlockState(blockX, blockHeight, blockZ, SAND);
                            for (int i = 0; i < (STONE_DEPTH + DIRT_DEPTH) / this.heightDivisor; i++) {
                                this.region.setBlockState(blockX, --blockHeight, blockZ, SAND);
                            }

                            // Set stone so sand doesn't fall
                            this.region.setBlockState(blockX, --blockHeight, blockZ, STONE);
                            continue;
                        }

                        int stoneChance = STONE_CHANCE;

                        // When we get to halfway above the world height, start reducing the visibility of grass
                        int halfwayPoint = (this.getHeight() / 2);
                        if (blockHeight > halfwayPoint) {
                            stoneChance = Math.max(STONE_CHANCE, GenericMath.floor(((double) ((blockHeight - halfwayPoint) * 2) / this.getHeight()) * 100));
                        }

                        // Don't place ground level if we have a schematic occupying the terrain or a network
                        if (occupyingSchematic == null && groundNetwork == null) {
                            this.region.setBlockState(blockX, blockHeight, blockZ, ThreadLocalRandom.current().nextInt(0, 100) <= stoneChance ? STONE : GRASS);
                        }

                        for (int i = 0; i < GenericMath.ceil((double) DIRT_DEPTH / this.heightDivisor); i++) {
                            int blockY = --blockHeight;

                            // Ensure we are not placing any terrain blocks on top of a schematic
                            if (occupiedPositions.contains(Vector3i.from(blockX, blockHeight, blockZ))) {
                                continue;
                            }

                            this.region.setBlockState(blockX, blockY, blockZ, DIRT);
                        }

                        for (int i = 0; i < STONE_DEPTH / this.heightDivisor; i++) {
                            int blockY = --blockHeight;

                            // Ensure we are not placing any terrain blocks on top of a schematic
                            if (occupiedPositions.contains(Vector3i.from(blockX, blockHeight, blockZ))) {
                                continue;
                            }

                            this.region.setBlockState(blockX, blockY, blockZ, STONE);
                        }
                    }
                }
            }
        }
    }

    public int getHeight(int x, int z) {
        return GenericMath.floor(this.heightMap.getHeight(x, z) / this.heightDivisor);
    }

    public int getInterpolatedHeight(int x, int z) {
        return GenericMath.floor(this.heightMap.getInterpolatedHeight(x, z) / this.heightDivisor);
    }

    public int getHeight() {
        return MAX_HEIGHT / this.heightDivisor;
    }

    private static Vector3i fixOccupiedChunkOffset(Vector3i pos, int rotation) {
        return switch (rotation) {
            case 90 -> pos.sub(0, 0, 1);
            case 180 -> pos.sub(1, 0, 1);
            case 270 -> pos.sub(1, 0, 0);
            default -> pos;
        };
    }
}
