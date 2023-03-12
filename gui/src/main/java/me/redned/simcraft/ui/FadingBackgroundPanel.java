package me.redned.simcraft.ui;

import me.redned.simcraft.SimCraftGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class FadingBackgroundPanel extends JPanel {
    private static final long DURATION = 5000;
    private static final long RUNNING_TIME = 2000;

    private static final List<BufferedImage> IMAGES = new ArrayList<>();
    private static final BufferedImage LOGO;

    static {
        try {
            IMAGES.add(ImageIO.read(SimCraftGUI.class.getResourceAsStream("/images/bg1.bmp")));
            IMAGES.add(ImageIO.read(SimCraftGUI.class.getResourceAsStream("/images/bg2.bmp")));
            IMAGES.add(ImageIO.read(SimCraftGUI.class.getResourceAsStream("/images/bg3.bmp")));

            LOGO = ImageIO.read(SimCraftGUI.class.getResourceAsStream("/simcraft.png"));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load background images for SimCraft GUI!", ex);
        }
    }

    private int currentImage;

    private float alpha = 0f;
    private long startTime = -1;

    public FadingBackgroundPanel() {
        final Timer timer = new Timer(40, e -> {
            if (startTime < 0) {
                startTime = System.currentTimeMillis();
            } else {

                long time = System.currentTimeMillis();
                long duration = time - startTime;
                if (duration >= RUNNING_TIME) {
                    startTime = -1;
                    ((Timer) e.getSource()).stop();
                    alpha = 0f;
                } else {
                    alpha = 1f - ((float) duration / (float) RUNNING_TIME);
                }
                repaint();
            }
        });

        new BackgroundWorker(timer).execute();
    }

    public BufferedImage getImage() {
        return IMAGES.get(this.currentImage);
    }

    public BufferedImage getNextImage() {
        return IMAGES.get(this.nextImage());
    }

    public int nextImage() {
        int nextImage = this.currentImage;
        if (nextImage >= IMAGES.size() - 1) {
            nextImage = 0;
        } else {
            nextImage++;
        }

        return nextImage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));

        BufferedImage image = this.getImage();
        g2d.drawImage(image.getSubimage(0, 0, image.getWidth(), image.getHeight() / 2), 0, 0, this.getWidth(), 150, null);

        g2d.setComposite(AlphaComposite.SrcOver.derive(1f - alpha));

        BufferedImage nextImage = this.getNextImage();
        g2d.drawImage(nextImage.getSubimage(0, 0, nextImage.getWidth(), nextImage.getHeight() / 2), 0, 0, this.getWidth(), 150, null);
        g2d.dispose();

        g.drawImage(LOGO, 0, 0, null);
    }

    private class BackgroundWorker extends SwingWorker<Void, Void> {
        private final Timer timer;

        public BackgroundWorker(Timer timer) {
            this.timer = timer;
        }

        @Override
        protected Void doInBackground() throws Exception {
            while (!isCancelled()) {
                alpha = 0f;

                currentImage = nextImage();

                // BufferedImage tmp = getImage();
                // image = imageOut;
                // imageOut = tmp;

                timer.start();

                Thread.sleep(DURATION);
            }

            return null;
        }
    }
}