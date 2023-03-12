package me.redned.simcraft;

import me.redned.simcraft.city.world.CityRegion;
import me.redned.simcraft.ui.BuildCitiesPanel;
import me.redned.simcraft.ui.CityProgressDialog;
import me.redned.simcraft.ui.FadingBackgroundPanel;
import me.redned.simcraft.ui.FileSelectionPanel;
import me.redned.simcraft.util.FileUtil;
import me.redned.simcraft.util.GameInstallUtil;
import me.redned.simreader.sc4.storage.exemplar.ExemplarFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

public class SimCraftGUI {

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        JFrame frame = new JFrame("SimCraft");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.requestFocus();
        frame.setIconImage(ImageIO.read(SimCraftGUI.class.getResourceAsStream("/simcraft-icon.png")));

        FadingBackgroundPanel backgroundPanel = new FadingBackgroundPanel();
        backgroundPanel.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight() / 3));
        frame.add(backgroundPanel, BorderLayout.PAGE_START);

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        JPanel selectionBox = new JPanel();
        selectionBox.setLayout(new BoxLayout(selectionBox, BoxLayout.Y_AXIS));

        Path installLocation = GameInstallUtil.getSimCityInstallLocation();

        CityBuilderContext context = new CityBuilderContext();
        if (installLocation != null) {
            context.setInstallDirectory(installLocation.toFile());
        }

        selectionBox.add(new FileSelectionPanel("Select SimCity city region directory", null, f -> null, context::setRegionDirectory));
        selectionBox.add(new FileSelectionPanel("Select Minecraft world export directory", null, f -> null, context::setExportDirectory));
        selectionBox.add(new FileSelectionPanel("Select SimCity install directory", installLocation != null ? installLocation.toString() : null, f -> {
            if (!new File(f, "SimCity_1.dat").exists()) {
                return "SimCity_1.dat file was not detected! Did you select the right directory?";
            }

            return null;
        }, context::setInstallDirectory));

        frame.add(selectionBox);
        frame.add(new BuildCitiesPanel(context, () -> {
            new Thread(() -> {
                buildCities(frame, context);
            }, "City Builder Thread").start();
        }), BorderLayout.PAGE_END);
        frame.setVisible(true);
    }

    private static void buildCities(JFrame frame, CityBuilderContext context) {
        File regionDirectory = context.getRegionDirectory();
        File exportDirectory = new File(context.getExportDirectory(), "SimCraft Cities");
        File installDirectory = context.getInstallDirectory();

        try {
            if (exportDirectory.exists()) {
                FileUtil.deleteDirectory(exportDirectory.toPath());
                exportDirectory.mkdirs();
            } else {
                exportDirectory.mkdirs();
            }

            CityProgressDialog progressDialog = new CityProgressDialog(frame);

            ExemplarFile exemplarFile = new ExemplarFile(installDirectory.toPath().resolve("SimCity_1.dat"));

            SimCraft simCraft = new SimCraft(regionDirectory.toPath(), exemplarFile, exportDirectory.toPath(), false);
            simCraft.buildRegions(progressDialog::setState);

            SwingUtilities.invokeLater(progressDialog::setSaving);
            simCraft.save();

            progressDialog.setVisible(false);

            StringBuilder builder = new StringBuilder();
            for (CityRegion region : simCraft.getLevel().getRegions()) {
                builder.append("\n")
                        .append("City \"")
                        .append(region.getCity().getName())
                        .append("\" can be found at coordinates: ")
                        .append(region.getMinPosition());
            }

            JOptionPane.showMessageDialog(frame, "Cities have completed exporting to a Minecraft world!\n" + builder, "Done!",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            ex.printStackTrace(printWriter);

            JOptionPane.showMessageDialog(frame, "An error occurred building the city. Please report this!\n\n" + stringWriter, "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static class CityBuilderContext {
        private File regionDirectory;
        private File exportDirectory;
        private File installDirectory;

        public File getRegionDirectory() {
            return regionDirectory;
        }

        public void setRegionDirectory(File regionDirectory) {
            this.regionDirectory = regionDirectory;
        }

        public File getExportDirectory() {
            return exportDirectory;
        }

        public void setExportDirectory(File exportDirectory) {
            this.exportDirectory = exportDirectory;
        }

        public File getInstallDirectory() {
            return installDirectory;
        }

        public void setInstallDirectory(File installDirectory) {
            this.installDirectory = installDirectory;
        }
    }
}
