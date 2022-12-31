package me.redned.simcraft.city.world;

import lombok.Getter;
import me.redned.levelparser.anvil.AnvilLevel;
import me.redned.levelparser.anvil.LevelData;
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

    public void buildRegions() {
        for (CityRegion region : this.regions) {
            region.buildCity();
        }
    }

    private AnvilLevel createLevel() {
        return new AnvilLevel(
                0,
                1024,
                0,
                true,
                new LevelData(
                        new LevelData.LevelVersion(
                                false,
                                "main",
                                3120,
                                "1.19.2"
                        ),
                        "City Region",
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
