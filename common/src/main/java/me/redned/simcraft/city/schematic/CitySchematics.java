package me.redned.simcraft.city.schematic;

import me.redned.simcraft.schematic.Schematic;
import me.redned.simcraft.util.FileUtil;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CitySchematics {
    private static final Map<String, Schematic> SCHEMATICS = new HashMap<>();

    static {
        load();
    }

    private static void load() {
        try {
            URI uri = CitySchematics.class.getResource("/schematics").toURI();
            FileUtil.forPathWithUri(uri, path -> {
                try {
                    loadSchematicsFromPath(path);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to load schematics!", ex);
                }
            });

            Path extPath = Paths.get("schematics");
            if (Files.exists(extPath)) {
                loadSchematicsFromPath(extPath);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load schematics!", ex);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Schematic getSchematic(String name) {
        return SCHEMATICS.get(name);
    }

    private static void loadSchematicsFromPath(Path path) throws IOException {
        try (Stream<Path> stream = Files.walk(path)) {
            stream.forEach(entry -> {
                if (Files.isDirectory(entry) || !entry.toString().endsWith("schem")) {
                    return;
                }

                try (NBTInputStream nbtStream = NbtUtils.createGZIPReader(Files.newInputStream(entry))) {
                    NbtMap root = (NbtMap) nbtStream.readTag();

                    String name = entry.getParent().relativize(entry).toString().replace(".schem", "");
                    Schematic schematic = Schematic.parse(root);

                    SCHEMATICS.put(name, schematic);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to parse schematic at location: " + path, ex);
                }
            });
        }
    }
}
