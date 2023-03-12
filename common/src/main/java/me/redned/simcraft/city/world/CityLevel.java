package me.redned.simcraft.city.world;

import lombok.Getter;
import me.redned.levelparser.anvil.AnvilLevel;
import me.redned.levelparser.anvil.LevelData;
import me.redned.simcraft.SimCraft;
import me.redned.simcraft.city.City;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class CityLevel {
    private final List<CityRegion> regions = new ArrayList<>();
    private final AnvilLevel level;

    public CityLevel(List<City> cities, boolean debug) {
        for (City city : cities) {
            this.regions.add(new CityRegion(city, this, debug));
        }

        this.level = this.createLevel();
    }

    public void buildRegions(SimCraft.RegionBuildState buildState) {
        buildState.setRegions(this.regions.size());
        for (CityRegion region : this.regions) {
            buildState.setCurrentRegion(this.regions.indexOf(region) + 1);
            buildState.setCityName(region.getCity().getName());
            buildState.setProgress(0);

            region.buildCity(buildState);
        }
    }

    private AnvilLevel createLevel() {
        return new AnvilLevel(
                0,
                1024,
                0,
                new LevelData(
                        new LevelData.LevelVersion(
                                false,
                                "main",
                                3218,
                                "1.19.3"
                        ),
                        "SimCraft Cities",
                        1,
                        10,
                        0,
                        10,
                        System.currentTimeMillis(),
                        false,
                        true,
                        List.of("vanilla"),
                        List.of(),
                        new LevelData.WorldGenSettings(
                                false,
                                0,
                                false,
                                Map.of("minecraft:overworld", NbtMap.builder()
                                        .putCompound("generator", NbtMap.builder()
                                                .putCompound("settings", NbtMap.builder()
                                                        .putBoolean("features", false)
                                                        .putString("biome", "minecraft:plains")
                                                        .putList("layers", NbtType.COMPOUND, NbtMap.builder()
                                                                .putString("block", "minecraft:air")
                                                                .putInt("height", 1)
                                                                .build())
                                                        .build())
                                                .putString("type", "minecraft:flat")
                                                .build())
                                        .putString("type", "minecraft:overworld")
                                        .build())
                        )
                )
        );
    }

}
