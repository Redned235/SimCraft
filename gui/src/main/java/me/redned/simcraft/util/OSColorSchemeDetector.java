package me.redned.simcraft.util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Adopted from: <a href="https://gist.github.com/HanSolo/7cf10b86efff8ca2845bf5ec2dd0fe1d">...</a>
 */
public final class OSColorSchemeDetector {
    public enum MacOSSystemColor {
        BLUE(new Color(0, 122, 255), new Color(10, 132, 255)),
        BROWN(new Color(162, 132, 94), new Color(172, 142, 104)),
        GRAY(new Color(142, 142, 147), new Color(152, 152, 157)),
        GREEN(new Color(40, 205, 65), new Color(50, 215, 75)),
        INIDIGO(new Color(88, 86, 214), new Color(94, 92, 230)),
        ORANGE(new Color(255, 149, 0), new Color(255, 159, 0)),
        PINK(new Color(255, 45, 85), new Color(255, 55, 95)),
        PURPLE(new Color(175, 82, 222), new Color(191, 90, 242)),
        RED(new Color(255, 59, 48), new Color(255, 69, 58)),
        TEAL(new Color(85, 190, 240), new Color(90, 200, 245)),
        YELLOW(new Color(255, 204, 0), new Color(255, 214, 10));

        final Color colorAqua;
        final Color colorDark;

        MacOSSystemColor(final Color colorAqua, final Color colorDark) {
            this.colorAqua = colorAqua;
            this.colorDark = colorDark;
        }

        public Color getColorAqua() {
            return colorAqua;
        }

        public Color getColorDark() {
            return colorDark;
        }

        public boolean isGivenColor(final Color color) {
            return (colorAqua.equals(color) || colorDark.equals(color));
        }

        public static List<MacOSSystemColor> getAsList() {
            return Arrays.asList(values());
        }
    }

    public enum MacOSAccentColor {
        MULTI_COLOR(null, MacOSSystemColor.BLUE.colorAqua, MacOSSystemColor.BLUE.colorDark),
        GRAPHITE(-1, MacOSSystemColor.GRAY.colorAqua, MacOSSystemColor.GRAY.colorDark),
        RED(0, MacOSSystemColor.RED.colorAqua, MacOSSystemColor.RED.colorDark),
        ORANGE(1, MacOSSystemColor.ORANGE.colorAqua, MacOSSystemColor.ORANGE.colorDark),
        YELLOW(2, MacOSSystemColor.YELLOW.colorAqua, MacOSSystemColor.YELLOW.colorDark),
        GREEN(3, MacOSSystemColor.GREEN.colorAqua, MacOSSystemColor.GREEN.colorDark),
        BLUE(4, MacOSSystemColor.BLUE.colorAqua, MacOSSystemColor.BLUE.colorDark),
        PURPLE(5, MacOSSystemColor.PURPLE.colorAqua, MacOSSystemColor.PURPLE.colorDark),
        PINK(6, MacOSSystemColor.PINK.colorAqua, MacOSSystemColor.PINK.colorDark);

        final Integer key;
        final Color colorAqua;
        final Color colorDark;

        MacOSAccentColor(final Integer key, final Color colorAqua, final Color colorDark) {
            this.key = key;
            this.colorAqua = colorAqua;
            this.colorDark = colorDark;
        }

        public Integer getKey() {
            return key;
        }

        public Color getColorAqua() {
            return colorAqua;
        }

        public Color getColorDark() {
            return colorDark;
        }

        public boolean isGivenColor(final Color color) {
            return (colorAqua.equals(color) || colorDark.equals(color));
        }

        public static List<MacOSAccentColor> getAsList() {
            return Arrays.asList(values());
        }
    }

    private static final String REGQUERY_UTIL = "reg query ";
    private static final String REGDWORD_TOKEN = "REG_DWORD";
    private static final String DARK_THEME_CMD = REGQUERY_UTIL + "\"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize\"" + " /v AppsUseLightTheme";

    public static final Map<Integer, Color[]> MACOS_ACCENT_COLOR_MAP = Map.of(
            -1, new Color[] { MacOSSystemColor.GRAY.colorAqua, MacOSSystemColor.GRAY.colorDark },
            0, new Color[] { MacOSSystemColor.RED.colorAqua, MacOSSystemColor.RED.colorDark },
            1, new Color[] { MacOSSystemColor.ORANGE.colorAqua, MacOSSystemColor.ORANGE.colorDark },
            2, new Color[] { MacOSSystemColor.YELLOW.colorAqua, MacOSSystemColor.YELLOW.colorDark },
            3, new Color[] { MacOSSystemColor.GREEN.colorAqua, MacOSSystemColor.GREEN.colorDark },
            4, new Color[] { MacOSSystemColor.BLUE.colorAqua, MacOSSystemColor.BLUE.colorDark },
            5, new Color[] { MacOSSystemColor.PURPLE.colorAqua, MacOSSystemColor.PURPLE.colorDark },
            6, new Color[] { MacOSSystemColor.PINK.colorAqua, MacOSSystemColor.PINK.colorDark });

    public static boolean isDarkMode() {
        return switch (OS.getOS()) {
            case WINDOWS -> isWindowsDarkMode();
            case MAC -> isMacOsDarkMode();
            default -> false;
        };
    }

    public static boolean isMacOsDarkMode() {
        try {
            boolean isDarkMode = false;
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("defaults read -g AppleInterfaceStyle");
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader rdr = new BufferedReader(isr);
            String line;
            while ((line = rdr.readLine()) != null) {
                if (line.equals("Dark")) {
                    isDarkMode = true;
                }
            }
            int rc = process.waitFor();  // Wait for the process to complete
            return 0 == rc && isDarkMode;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public static boolean isWindowsDarkMode() {
        try {
            Process process = Runtime.getRuntime().exec(DARK_THEME_CMD);
            StreamReader reader = new StreamReader(process.getInputStream());

            reader.start();
            process.waitFor();
            reader.join();

            String result = reader.getResult();
            int p = result.indexOf(REGDWORD_TOKEN);

            if (p == -1) {
                return false;
            }

            // 1 == Light Mode, 0 == Dark Mode
            String temp = result.substring(p + REGDWORD_TOKEN.length()).trim();
            return ((Integer.parseInt(temp.substring("0x".length()), 16))) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static Color getMacOSAccentColor() {
        if (OS.MAC != OS.getOS()) {
            return MacOSAccentColor.MULTI_COLOR.getColorAqua();
        }
        final boolean isDarkMode = isMacOsDarkMode();
        try {
            Integer colorKey = null;
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("defaults read -g AppleAccentColor");
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader rdr = new BufferedReader(isr);
            String line;
            while ((line = rdr.readLine()) != null) {
                colorKey = Integer.valueOf(line);
            }
            int rc = process.waitFor();  // Wait for the process to complete
            if (0 == rc) {
                return isDarkMode ? MACOS_ACCENT_COLOR_MAP.get(colorKey)[1] : MACOS_ACCENT_COLOR_MAP.get(colorKey)[0];
            } else {
                return isDarkMode ? MACOS_ACCENT_COLOR_MAP.get(4)[1] : MACOS_ACCENT_COLOR_MAP.get(4)[0];
            }
        } catch (IOException | InterruptedException e) {
            return isDarkMode ? MACOS_ACCENT_COLOR_MAP.get(4)[1] : MACOS_ACCENT_COLOR_MAP.get(4)[0];
        }
    }

    // ******************** Internal Classes **********************************
    static class StreamReader extends Thread {
        private final InputStream is;
        private final StringWriter sw;

        StreamReader(InputStream is) {
            this.is = is;
            sw = new StringWriter();
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
            } catch (IOException ignored) {
            }
        }

        String getResult() {
            return sw.toString();
        }
    }
}