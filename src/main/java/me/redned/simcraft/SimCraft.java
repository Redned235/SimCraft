package me.redned.simcraft;

import lombok.Getter;
import me.redned.levelparser.LevelParser;
import me.redned.levelparser.anvil.AnvilLevel;
import me.redned.levelparser.anvil.io.AnvilLevelWriter;
import me.redned.simcraft.city.City;
import me.redned.simcraft.city.world.CityLevel;
import me.redned.simcraft.util.FileUtil;
import me.redned.simreader.sc4.storage.SC4File;
import me.redned.simreader.sc4.storage.exemplar.ExemplarFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class SimCraft {
    private static final String SAVEGAME_EXTENSION = ".sc4";

    private final List<City> cities;
    private final CityLevel level;

    private final LevelParser<AnvilLevel> parser;
    private final Path outputPath;

    public SimCraft(Path cityPath, ExemplarFile exemplarFile, Path outputPath, boolean debug) throws IOException {
        List<City> cities = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(cityPath)) {
            paths.forEach(path -> {
                if (path.toString().endsWith(SAVEGAME_EXTENSION)) {
                    try {
                        cities.add(new City(new SC4File(path), exemplarFile));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }

        this.cities = cities;
        this.level = new CityLevel(cities, debug);

        this.parser = LevelParser.<AnvilLevel>builder()
                .output(outputPath)
                .writer(new AnvilLevelWriter())
                .build();

        this.outputPath = outputPath;
    }

    public void buildRegions() {
        this.level.buildRegions();
    }

    public void save() throws IOException {
        this.parser.writeLevel(this.level.getLevel());

        // Include our datapack that increases the world height
        try {
            Path scDatapackPath = this.outputPath.resolve("datapacks/simcraft");
            Files.createDirectories(scDatapackPath);

            URI uri = this.getClass().getResource("/packs/datapack/").toURI();
            FileUtil.forPathWithUri(uri, path -> {
                try {
                    FileUtil.copyRecursively(path, scDatapackPath);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to copy file with path " + path, ex);
                }
            });
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Failed to load datapacks!", ex);
        }
    }
}
