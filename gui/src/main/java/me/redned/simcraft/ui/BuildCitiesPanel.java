package me.redned.simcraft.ui;

import me.redned.simcraft.SimCraftGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class BuildCitiesPanel extends JPanel {

    public BuildCitiesPanel(SimCraftGUI.CityBuilderContext context, Runnable cityBuilderRunnable) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(500, 50));
        button.setText("Export to Minecraft World");
        this.add(button);

        button.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                File regionDirectory = context.getRegionDirectory();
                File exportDirectory = context.getExportDirectory();
                File installDirectory = context.getInstallDirectory();
                if (regionDirectory == null || !regionDirectory.exists()) {
                    JOptionPane.showMessageDialog(new JFrame(), "The selected SimCity region directory does not exist!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (exportDirectory == null || !exportDirectory.exists()) {
                    JOptionPane.showMessageDialog(new JFrame(), "The selected Minecraft world export directory does not exist!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (installDirectory == null || !installDirectory.exists()) {
                    JOptionPane.showMessageDialog(new JFrame(), "The selected SimCity install directory does not exist!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                cityBuilderRunnable.run();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }
}
