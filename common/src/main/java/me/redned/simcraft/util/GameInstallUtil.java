package me.redned.simcraft.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameInstallUtil {
    private static final int SIM_CITY_4_GID = 24780;

    public static Path getSimCityInstallLocation() {
        try {
            if (OS.getOS() != OS.WINDOWS) {
                return null;
            }

            String steamLocation = ConsoleReader.readFromConsole("reg query HKCU\\SOFTWARE\\Valve\\Steam /v SteamPath");
            if (steamLocation.isBlank()) {
                return null;
            }

            String installLoc = steamLocation.split("REG_SZ")[1].trim();
            Path path = Paths.get(installLoc, "steamapps", "libraryfolders.vdf");

            Path steamPath = null;
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                JSONObject jsonObject = VDF.toJSONObject(new JSONTokener(reader), true);
                JSONArray libraryFolders = jsonObject.getJSONArray("libraryfolders");
                for (Object obj : libraryFolders) {
                    JSONObject libraryObject = (JSONObject) obj;
                    JSONObject appsObject = libraryObject.getJSONObject("apps");
                    if (appsObject.has(Integer.toString(SIM_CITY_4_GID))) {
                        steamPath = Paths.get(libraryObject.getString("path"));
                    }
                }
            }

            if (steamPath != null) {
                return steamPath.resolve("steamapps/common/SimCity 4 Deluxe/");
            }
        } catch (Exception ex) {
            System.err.println("Failed to automatically find SimCity 4 install location!");
            ex.printStackTrace();
        }

        return null;
    }
}
