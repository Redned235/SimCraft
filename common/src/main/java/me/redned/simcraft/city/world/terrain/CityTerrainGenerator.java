package me.redned.simcraft.city.world.terrain;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.redned.levelparser.BlockState;
import me.redned.simcraft.SimCraft;
import me.redned.simcraft.city.City;
import me.redned.simcraft.city.lot.LotData;
import me.redned.simcraft.city.network.NetworkData;
import me.redned.simcraft.city.placeable.BuildingData;
import me.redned.simcraft.city.schematic.CitySchematics;
import me.redned.simcraft.city.world.CityRegion;
import me.redned.simcraft.city.world.network.piece.NetworkPiece;
import me.redned.simcraft.schematic.Schematic;
import me.redned.simcraft.util.collection.ThreeDimensionalPositionList;
import me.redned.simcraft.util.collection.TwoDimensionalPositionMap;
import me.redned.simcraft.util.heightmap.HeightMap;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static final int THREAD_COUNT = 5;

    private final CityRegion region;

    @Getter
    private final int heightDivisor;

    @Getter
    private HeightMap heightMap;

    private final TwoDimensionalPositionMap<ObjectIntPair<Schematic>> tileSchematics = new TwoDimensionalPositionMap<>();

    public void buildTerrain(SimCraft.RegionBuildState buildState) {
        buildState.setBuildState(SimCraft.BuildState.TERRAIN);
        buildState.setProgress(0);

        // TODO: This is very fast but still takes a long time for larger cities.
        //       Would definitely benefit from Java 21's virtual threads due to the
        //       very high throughput this has.
        int threads = Runtime.getRuntime().availableProcessors() * 3 / 8;
        ExecutorService threadPool = Executors.newFixedThreadPool(threads, new ThreadFactory() {
            private final AtomicInteger threadCounter = new AtomicInteger();

            @Override
            public Thread newThread(final Runnable run) {
                final Thread ret = new Thread(run);

                ret.setName("CityTerrainGenerator #" + this.threadCounter.getAndIncrement());
                ret.setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());

                return ret;
            }
        });

        City city = this.region.getCity();
        float[][] rawHeightMap = city.getHeightMap().clone();

        // Sync our lot heights
        this.region.getLots().forEach((x, y, lot) -> rawHeightMap[y][x] = lot.getYPosition());

        // Sync our network heights
        this.region.getNetworkBuilder().getGroundLevelNetwork().forEach((x, y, network) -> rawHeightMap[y][x] = network.getPosition().getY() - 2);

        // Go through our buildings and see if there are any that
        // will be placed inside the terrain (i.e. agriculture plots)
        for (BuildingData building : city.getBuildings()) {
            Schematic schematic = CitySchematics.getSchematic(building.getIdentifier());
            if (schematic != null) {
                if (schematic.getMetadata().getBoolean("SCTerrainBlend", false)) {
                    // Blend the chunk data with the terrain
                    this.tileSchematics.put((int) building.getMinPosition().getX() >> 4, (int) building.getMinPosition().getZ() >> 4, ObjectIntPair.of(schematic, building.getRotation()));
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
        AtomicInteger itr = new AtomicInteger(0);
        double maxProgress = (rawHeightMap.length - 1) * (rawHeightMap.length - 1);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int cz = 0; cz < rawHeightMap.length - 1; cz++) {
            float[] zPositions = rawHeightMap[cz];
            for (int cx = 0; cx < zPositions.length - 1; cx++) {
                int chunkX = cx;
                int chunkZ = cz;

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    // Check if we have a schematic occupying this chunk. This allows the terrain generator
                    // to use blocks from the schematic rather than the default ones. Main purpose for this is to
                    // allow for certain tiles (i.e. agriculture tiles) to blend in with the terrain, so they don't look
                    // like terraces.
                    ThreeDimensionalPositionList occupiedPositions;
                    ObjectIntPair<Schematic> occupyingSchematic = this.tileSchematics.get(chunkX, chunkZ);
                    if (occupyingSchematic != null) {
                        occupiedPositions = new ThreeDimensionalPositionList();
                        occupyingSchematic.key().paste(this.region.getLevel().getLevel(), Vector3i.from(chunkX << 4, 0, chunkZ << 4), occupyingSchematic.valueInt(), (initialPos, schemPos) -> {
                            int blockX = schemPos.getX();
                            int blockZ = schemPos.getZ();

                            float height = heightMap.getData()[blockZ][blockX] / this.heightDivisor;
                            occupiedPositions.add(blockX, GenericMath.floor(height + initialPos.getY()), blockZ);
                            return Vector3i.from(
                                    blockX + this.region.getMinPosition().getX(),
                                    height + initialPos.getY(),
                                    blockZ +  this.region.getMinPosition().getY()
                            );
                        }, false, false);
                    } else {
                        occupiedPositions = null;
                    }

                    NetworkData groundNetwork = this.region.getNetworkBuilder().getGroundNetwork(chunkX, chunkZ);

                    LotData lot = this.region.getLot(chunkX, chunkZ);
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int blockX = (chunkX << 4) + x;
                            int blockZ = (chunkZ << 4) + z;
                            float height = (lot != null && occupyingSchematic == null) ? lot.getYPosition() : heightMap.getHeight(blockX, blockZ);

                            // Update our heightmap (needed for lot retaining walls)
                            heightMap.getData()[blockZ][blockX] = height;

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
                                if (occupiedPositions != null && occupiedPositions.contains(blockX, blockHeight, blockZ)) {
                                    continue;
                                }

                                this.region.setBlockState(blockX, blockY, blockZ, DIRT);
                            }

                            for (int i = 0; i < STONE_DEPTH / this.heightDivisor; i++) {
                                int blockY = --blockHeight;

                                // Ensure we are not placing any terrain blocks on top of a schematic
                                if (occupiedPositions != null && occupiedPositions.contains(blockX, blockHeight, blockZ)) {
                                    continue;
                                }

                                this.region.setBlockState(blockX, blockY, blockZ, STONE);
                            }
                        }
                    }

                    buildState.setProgress(itr.getAndIncrement() / maxProgress);
                }, threadPool);

                futures.add(future);
            }
        }

        CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        try {
            future.get(5, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            throw new RuntimeException("Failed to generate terrain", ex);
        }

        threadPool.shutdown();
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
}
