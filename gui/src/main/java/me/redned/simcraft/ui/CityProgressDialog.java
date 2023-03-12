package me.redned.simcraft.ui;

import me.redned.simcraft.SimCraft;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class CityProgressDialog extends JDialog {
    private BackgroundWorker worker;

    public CityProgressDialog(JFrame frame) {
        this.setLayout(new FlowLayout());
        this.setSize(200, 110);
        this.setLocationRelativeTo(frame);
    }

    public void setState(SimCraft.RegionBuildState state) {
        this.setVisible(true);

        JLabel stageLabel = new JLabel("Initializing...");
        this.add(stageLabel);

        JProgressBar progressBar = new JProgressBar(0, 100);
        this.add(progressBar);

        JLabel progressLabel = new JLabel("Progress: 0%");
        this.add(progressLabel);

        this.worker = new BackgroundWorker(state, progressBar, stageLabel, progressLabel);
        this.worker.execute();
    }

    public void setSaving() {
        if (this.worker != null) {
            this.worker.cancel(true);
        }

        this.removeAll();
        this.setLayout(new FlowLayout());
        this.add(new JLabel("Saving world..."));
    }

    private class BackgroundWorker extends SwingWorker<Void, Void> {

        private final SimCraft.RegionBuildState state;
        private final JProgressBar progressBar;
        private final JLabel stateLabel;
        private final JLabel progressLabel;

        public BackgroundWorker(SimCraft.RegionBuildState state, JProgressBar progressBar, JLabel stateLabel, JLabel progressLabel) {
            this.state = state;
            this.progressBar = progressBar;
            this.stateLabel = stateLabel;
            this.progressLabel = progressLabel;
        }

        @Override
        protected Void doInBackground() throws Exception {
            while (!isCancelled()) {
                CityProgressDialog.this.setTitle("City: " + this.state.getCityName() + " (" + this.state.getCurrentRegion() + "/" + this.state.getRegions() + ")");
                this.stateLabel.setText("Building " + this.state.getBuildState().name().toLowerCase(Locale.ROOT) + "...");
                this.progressLabel.setText("Stage progress: " + ((int) (this.state.getProgress() * 100) + "%"));

                this.progressBar.setValue((int) (this.state.getProgress() * 100));

                Thread.sleep(50);
            }

            return null;
        }
    }
}
