package me.redned.simcraft.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

public class FileSelectionPanel extends JPanel {

    public FileSelectionPanel(String panelName, String defaultFileLocation, Function<File, String> fileErrorTest, Consumer<File> fileConsumer) {
        this.setLayout(new BorderLayout());

        JPanel labelPanel = new JPanel();
        JLabel label = new JLabel(panelName);
        label.setPreferredSize(new Dimension(350, 15));
        labelPanel.add(label, BorderLayout.WEST);

        this.add(labelPanel, BorderLayout.PAGE_START);

        JPanel selectionPanel = new JPanel();

        JTextField textField = new JTextField(defaultFileLocation);
        textField.setColumns(35);

        Runnable textChangeRunnable = () -> {
            File file = new File(textField.getText());
            if (file.exists() && file.isDirectory() && fileErrorTest.apply(file) == null) {
                fileConsumer.accept(file);
            }
        };

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textChangeRunnable.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                textChangeRunnable.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                textChangeRunnable.run();
            }
        });

        selectionPanel.add(textField);

        JButton openButton = new JButton("Open");
        openButton.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                File file = new File(defaultFileLocation == null ? "" : defaultFileLocation);

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                if (file.exists()) {
                    fileChooser.setSelectedFile(file);
                }

                fileChooser.showOpenDialog(openButton);

                File result = fileChooser.getSelectedFile();
                if (result == null) {
                    return;
                }

                if (!result.isDirectory()) {
                    JOptionPane.showMessageDialog(new JFrame(), "Your selection must be a directory!", "Error!",
                            JOptionPane.ERROR_MESSAGE);
                }

                String error = fileErrorTest.apply(result);
                if (error != null) {
                    JOptionPane.showMessageDialog(new JFrame(), error, "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                textField.setText(result.toString());
                fileConsumer.accept(result);
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

        selectionPanel.add(openButton);

        this.add(selectionPanel, BorderLayout.CENTER);
    }
}
