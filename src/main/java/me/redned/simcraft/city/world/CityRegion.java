package me.redned.simcraft.city.world;

import lombok.Getter;
import me.redned.levelparser.BlockState;
import me.redned.levelparser.Chunk;
import me.redned.simcraft.city.City;
import me.redned.simcraft.city.lot.LotData;
import me.redned.simcraft.city.placeable.PlaceableData;
import me.redned.simcraft.city.placeable.PropData;
import me.redned.simcraft.city.schematic.CitySchematics;
import me.redned.simcraft.city.world.network.CityNetworkBuilder;
import me.redned.simcraft.city.world.terrain.CityTerrainGenerator;
import me.redned.simcraft.schematic.Schematic;
import me.redned.simcraft.util.collection.TwoDimensionalPositionMap;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;

import java.util.List;

@Getter
public class CityRegion {
    private static final int HEIGHT_DIVISOR = 2;
    private static final int REGION_TILE_SIZE = 64;

    private final City city;
    private final CityLevel level;

    private final CityTerrainGenerator terrainGenerator;
    private final CityNetworkBuilder networkBuilder;

    private final TwoDimensionalPositionMap<LotData> lots = new TwoDimensionalPositionMap<>();

    private final Vector2i minPosition;

    private final boolean debug;

    public CityRegion(City city, CityLevel level, boolean debug) {
        this.city = city;
        this.level = level;

        this.terrainGenerator = new CityTerrainGenerator(this, HEIGHT_DIVISOR);
        this.networkBuilder = new CityNetworkBuilder(this, this.terrainGenerator);

        this.debug = debug;

        for (LotData lot : city.getLots()) {
            for (int chunkX = lot.getMinTilePosition().getX(); chunkX < lot.getMinTilePosition().getX() + lot.getDimensions().getX(); chunkX++) {
                for (int chunkZ = lot.getMinTilePosition().getY(); chunkZ < lot.getMinTilePosition().getY() + lot.getDimensions().getY(); chunkZ++) {
                    if (chunkX < 0 || chunkZ < 0) {
                        System.err.println("Invalid lot position: " + chunkX + " " + chunkZ);
                        continue;
                    }

                    this.lots.put(chunkX, chunkZ, lot);
                }
            }
        }

        this.minPosition = Vector2i.from((this.getTilePosition().getX() * REGION_TILE_SIZE) << 4, (this.getTilePosition().getY() * REGION_TILE_SIZE) << 4);
    }

    public void buildCity() {
        // Build terrain first
        this.terrainGenerator.buildTerrain();

        // Build transportation networks
        this.networkBuilder.buildNetworks();

        // Build placeables
        this.buildPlaceables(this.city.getFlora(), false);
        this.buildPlaceables(this.city.getProps(), false);
        this.buildPlaceables(this.city.getBuildings(), true);
    }

    private void buildPlaceables(List<? extends PlaceableData> placeables, boolean pasteAir) {
        for (PlaceableData placeable : placeables) {
            if (!placeable.shouldDisplay()) {
                continue;
            }

            Vector3i minPos = placeable.getMinPosition().div(1, HEIGHT_DIVISOR, 1).add(0, 1, 0).toInt();
            Vector3i maxPos = placeable.getMaxPosition().div(1, HEIGHT_DIVISOR, 1).add(0, 1, 0).toInt();

            Schematic schematic = CitySchematics.getSchematic(placeable.getIdentifier());
            if (schematic != null) {
                // Blended terrain is already built
                if (schematic.getMetadata().getBoolean("SCTerrainBlend", false)) {
                    continue;
                }

                Vector3i schemPos = minPos;

                // If we need to occupy the chunk, always paste the schematic at the min
                // position of the current chunk the schematic is being pasted in to. This
                // flag is primarily used for "greedy" tiles such as agriculture zoning that
                // places farmland by default if there aren't other buildings (i.e. a barn)
                boolean occupyChunk = schematic.getMetadata().getBoolean("SCOccupyChunk", false);
                if (occupyChunk) {
                    schemPos = Vector3i.from((schemPos.getX() << 4) >> 4, schemPos.getY(), (schemPos.getZ() << 4) >> 4);
                }

                int offsetX = schematic.getMetadata().getInt("SCOffsetX", 0);
                int offsetY = schematic.getMetadata().getInt("SCOffsetY", 0);
                int offsetZ = schematic.getMetadata().getInt("SCOffsetZ", 0);
                pasteAtOptimalPosition(schemPos, placeable, schematic, pasteAir, placeable.getRotation(), offsetX, offsetY, offsetZ);
            } else {
                for (int x = minPos.getX(); x < maxPos.getX(); x++) {
                    for (int y = minPos.getY(); y < maxPos.getY(); y++) {
                        for (int z = minPos.getZ(); z < maxPos.getZ(); z++) {
                            this.setBlockState(x, y, z, BlockState.of("minecraft:scaffolding"));
                        }
                    }
                }
            }

            if (this.debug) {
                Chunk chunk = this.getChunk(minPos.getX() >> 4, minPos.getZ() >> 4);
                chunk.getBlockEntities().add(NbtMap.builder()
                        .putString("id", "minecraft:sign")
                        .putInt("x", minPos.getX() + this.getMinPosition().getX())
                        .putInt("y", maxPos.getY())
                        .putInt("z", minPos.getZ() + this.getMinPosition().getY())
                        .putString("Text1", "{\"text\":\"" + placeable.getIdentifier() + "\"}")
                        .putString("Text2", "{\"text\":\"" + placeable.getOrientation() + " (" + placeable.getRotation() + ")" + "\"}")
                        .build());

                this.setBlockState(minPos.getX(), maxPos.getY(), minPos.getZ(), BlockState.of("minecraft:oak_sign"));
            }
        }
    }

    public LotData getLot(int tileX, int tileZ) {
        return this.lots.get(tileX, tileZ);
    }

    public Vector2i getTilePosition() {
        return this.city.getTilePosition();
    }

    public Chunk getChunk(int x, int z) {
        Vector2i position = this.getTilePosition();
        return this.level.getLevel().getChunk((position.getX() * REGION_TILE_SIZE) + x, (position.getY() * REGION_TILE_SIZE) + z);
    }

    public BlockState getBlockState(int x, int y, int z) {
        Vector2i minPosition = this.getMinPosition();
        return this.level.getLevel().getBlockState(minPosition.getX() + x, y, minPosition.getY() + z);
    }

    public void setBlockState(int x, int y, int z, BlockState state) {
        Vector2i minPosition = this.getMinPosition();
        this.level.getLevel().setBlockState(minPosition.getX() + x, y, minPosition.getY() + z, state);
    }

    private void pasteAtOptimalPosition(Vector3i pos, PlaceableData placeable, Schematic schematic, boolean pasteAir, int rotation, int offsetX, int offsetY, int offsetZ) {
        pasteAtOptimalPosition(pos, placeable, schematic, pasteAir, rotation, offsetX, offsetY, offsetZ, 0);
    }

    private void pasteAtOptimalPosition(Vector3i pos, PlaceableData placeable, Schematic schematic, boolean pasteAir, int rotation, int offsetX, int offsetY, int offsetZ, int iterations) {
        if (iterations > 10) {
            System.err.println("Attempted to paste schematic below terrain!");
            return;
        }

        Vector3d centerPos = schematic.getCenterPosition();
        if (this.getBlockState(pos.getX() + (int) centerPos.getX(), pos.getY() - 1, pos.getZ() + (int) centerPos.getZ()).equals(BlockState.AIR)) {
            pasteAtOptimalPosition(pos.sub(0, 1, 0), placeable, schematic, pasteAir, rotation, offsetX, offsetY, offsetZ, iterations + 1);
            return;
        }

        Vector2i min = this.getMinPosition();
        schematic.paste(this.level.getLevel(), pos.add(offsetX, offsetY, offsetZ).add(min.getX(), 0, min.getY()), rotation, null, pasteAir, placeable instanceof PropData);
    }
}
