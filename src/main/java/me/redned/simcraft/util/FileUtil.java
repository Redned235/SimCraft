package me.redned.simcraft.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FileUtil {

    public static void copyRecursively(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(path -> {
                try {
                    Files.copy(path, target.resolve(source.relativize(path).toString()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to copy " + path + "!", ex);
                }
            });
        }
    }

    public static void deleteDirectory(Path directory) throws IOException {
        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
    }

    public static void forPathWithUri(URI uri, Consumer<Path> path) {
        // Locally ran
        if (uri.getScheme().equals("file")) {
            path.accept(Paths.get(uri));
            return;
        }

        String[] split = uri.toString().split("!");
        try (FileSystem system = FileSystems.newFileSystem(URI.create(split[0]), Map.of("create", "true"))) {
            path.accept(system.getPath(split[1]));
        } catch (IOException ex) {
            throw new RuntimeException("Error handling file with URI: " + uri, ex);
        }
    }

}
