package me.redned.simcraft;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import me.redned.simcraft.city.City;
import me.redned.simcraft.city.placeable.BuildingData;
import me.redned.simcraft.city.placeable.FloraData;
import me.redned.simcraft.city.placeable.PropData;
import me.redned.simcraft.city.schematic.CitySchematics;
import me.redned.simcraft.city.world.CityRegion;
import me.redned.simcraft.util.FileUtil;
import me.redned.simcraft.util.GameInstallUtil;
import me.redned.simcraft.util.OS;
import me.redned.simreader.sc4.storage.exemplar.ExemplarFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Bootstrap {
    private static final OptionParser PARSER = new OptionParser();

    private static final OptionSpec<Void> HELP_SPEC = PARSER.acceptsAll(List.of("?", "h", "help"), "Shows the help menu.").forHelp();
    private static final OptionSpec<Path> CITY_PATH_SPEC = PARSER.acceptsAll(List.of("c", "cities"), "Location of cities to convert.")
            .requiredUnless("help")
            .withRequiredArg()
            .withValuesConvertedBy(new PathConverter());

    private static final OptionSpec<Path> GAME_DIR_PATH_SPEC = PARSER.acceptsAll(List.of("g", "gamedir"), "Location of SimCity 4 game directory.")
            .withOptionalArg()
            .withValuesConvertedBy(new PathConverter());

    private static final OptionSpec<Path> OUTPUT_DIR_PATH_SPEC = PARSER.acceptsAll(List.of("o", "output"), "Output location of the Minecraft world.")
            .withOptionalArg()
            .withValuesConvertedBy(new PathConverter())
            .defaultsTo(Paths.get("output"));

    private static final OptionSpec<Void> DEBUG_SPEC = PARSER.acceptsAll(List.of("d", "debug"), "Enables debug mode.");
    private static final OptionSpec<Void> PRINT_MISSING_SPEC = PARSER.acceptsAll(List.of("pm", "print-missing"), "Prints various missing information.");

    public static void main(String[] args) throws IOException {
        OptionSet optionSet = PARSER.parse(args);
        if (optionSet.has(HELP_SPEC)) {
            PARSER.printHelpOn(System.out);
            return;
        }

        Path gameDir = null;
        if (optionSet.has(GAME_DIR_PATH_SPEC)) {
            gameDir = optionSet.valueOf(GAME_DIR_PATH_SPEC);
        } else {
            if (OS.getOS() == OS.WINDOWS) {
                gameDir = GameInstallUtil.getSimCityInstallLocation();
            }

            if (gameDir == null) {
                throw new RuntimeException("SimCity game directory was not entered & could not be found automatically!");
            }
        }

        boolean debug = optionSet.has(DEBUG_SPEC);
        if (debug) {
            System.out.println("Debug mode enabled!");
        }

        Path citiesDir = optionSet.valueOf(CITY_PATH_SPEC);
        if (Files.notExists(citiesDir) || !Files.isDirectory(citiesDir)) {
            throw new RuntimeException("City directory was not found or was not a directory!");
        }

        Path exemplarPath = gameDir.resolve("SimCity_1.dat");
        if (Files.notExists(exemplarPath)) {
            throw new RuntimeException("SimCity_1.dat exemplar file not found in specified game directory! Ensure this file exists in the directory you provided!");
        }

        Path outputDir = optionSet.valueOf(OUTPUT_DIR_PATH_SPEC);
        if (Files.exists(outputDir)) {
            System.out.println("Found existing output directory, clearing...");
            FileUtil.deleteDirectory(outputDir);
        }

        Files.createDirectories(outputDir);

        ExemplarFile exemplarFile = new ExemplarFile(exemplarPath);

        SimCraft simCraft = new SimCraft(citiesDir, exemplarFile, outputDir, debug);
        if (optionSet.has(PRINT_MISSING_SPEC)) {
            List<String> missingPlaceables = new ArrayList<>();
            for (CityRegion region : simCraft.getLevel().getRegions()) {
                City city = region.getCity();
                for (BuildingData building : city.getBuildings()) {
                    if (CitySchematics.getSchematic(building.getIdentifier()) == null) {
                        missingPlaceables.add("Building - " + building.getIdentifier());
                    }
                }

                for (PropData prop : city.getProps()) {
                    if (CitySchematics.getSchematic(prop.getIdentifier()) == null) {
                        missingPlaceables.add("Prop - " + prop.getIdentifier());
                    }
                }

                for (FloraData flora : city.getFlora()) {
                    if (CitySchematics.getSchematic(flora.getIdentifier()) == null) {
                        missingPlaceables.add("Flora - " + flora.getIdentifier());
                    }
                }

                System.out.println("City \"" + region.getCity().getName() + "\" can be found at coordinates: " + region.getMinPosition());
            }

            Map<String, Long> missingPlaceableCounts = missingPlaceables.stream()
                    .collect(Collectors.groupingBy(e -> e, LinkedHashMap::new, Collectors.counting()));

            if (missingPlaceableCounts.isEmpty()) {
                System.out.println("No missing schematics!");
            } else {
                System.out.println(missingPlaceableCounts.size() + " missing schematics:");
            }

            List<Map.Entry<String, Long>> entries = missingPlaceableCounts.entrySet()
                    .stream()
                    .sorted(Comparator.<Map.Entry<String, Long>>comparingInt(e -> Math.toIntExact(e.getValue())).reversed())
                    .toList();

            entries.forEach(entry -> {
                System.out.println(entry.getKey() + " x" + entry.getValue());
            });

            return;
        }

        System.out.println("Building city regions...");
        simCraft.buildRegions((state) -> { });

        System.out.println("Saving...");
        simCraft.save();

        System.out.println("Done!");

        for (CityRegion region : simCraft.getLevel().getRegions()) {
            System.out.println("City \"" + region.getCity().getName() + "\" can be found at coordinates: " + region.getMinPosition());
        }
    }
}
