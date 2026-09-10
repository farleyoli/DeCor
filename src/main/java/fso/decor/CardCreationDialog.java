package fso.decor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class CardCreationDialog {

    static List<Pair<String, String>> showQuestionDialog() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width / 3;
        int height = screenSize.height / 3;
        return show("Create Card", "Question:", "Extra:", null, width, height);
    }

    static List<Pair<String, String>> showImageQuestionDialog(File imageFile) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width / 3;
        int height = screenSize.height / 3;
        return show("Create Card (Image Question)", "Question text (optional):", "Answer:", imageFile, width, height);
    }

    private static List<Pair<String, String>> show(String title, String frontLabel, String backLabel,
                                                   File imageFile, int width, int height) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1;

        int gridy = 0;

        if (imageFile != null) {
            try {
                BufferedImage img = ImageIO.read(imageFile);
                int previewWidth = Math.min(width - 20, img.getWidth());
                int previewHeight = (int) ((double) img.getHeight() / img.getWidth() * previewWidth);
                if (previewHeight > height / 2) {
                    previewHeight = height / 2;
                    previewWidth = (int) ((double) img.getWidth() / img.getHeight() * previewHeight);
                }
                Image scaled = img.getScaledInstance(previewWidth, previewHeight, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaled));
                imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                gbc.gridx = 0; gbc.gridy = gridy++; gbc.weighty = 0;
                gbc.fill = GridBagConstraints.NONE;
                panel.add(imageLabel, gbc);
            } catch (IOException e) {
                JLabel errorLabel = new JLabel("(image preview unavailable)");
                gbc.gridx = 0; gbc.gridy = gridy++; gbc.weighty = 0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                panel.add(errorLabel, gbc);
            }
        }

        JPanel rowsPanel = new JPanel();
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        JScrollPane rowsScroll = new JScrollPane(rowsPanel);
        rowsScroll.setPreferredSize(new Dimension(width, height));
        rowsScroll.setBorder(null);
        rowsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        gbc.gridx = 0; gbc.gridy = gridy++; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(rowsScroll, gbc);

        List<Row> rows = new ArrayList<>();
        Runnable addRow = () -> {
            if (!rows.isEmpty()) {
                rowsPanel.add(new JSeparator());
            }
            Row row = new Row(frontLabel, backLabel, width, height);
            rows.add(row);
            rowsPanel.add(row);
            rowsPanel.revalidate();
            rowsPanel.repaint();
            SwingUtilities.invokeLater(() -> {
                JScrollBar sb = rowsScroll.getVerticalScrollBar();
                sb.setValue(sb.getMaximum());
            });
        };
        addRow.run();

        JButton plusButton = new PlusButton();
        plusButton.addActionListener(e -> addRow.run());
        plusButton.setToolTipText("Add another card");
        gbc.gridx = 0; gbc.gridy = gridy++; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(plusButton, gbc);

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

        float opacity = GlobalConfig.getDialogOpacity();
        JDialog dialog;
        if (opacity < 1.0f) {
            dialog = new JDialog((Frame) null, title, true);
            dialog.setUndecorated(true);
            dialog.setContentPane(pane);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setOpacity(opacity);
            pane.addPropertyChangeListener(e -> {
                if (dialog.isVisible() && e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
                    dialog.setVisible(false);
                }
            });
        } else {
            dialog = pane.createDialog(null, title);
        }

        dialog.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                rows.get(0).frontField.requestFocus();
            }
        });

        dialog.setVisible(true);

        Object selectedValue = pane.getValue();
        int result = (selectedValue == null ? JOptionPane.CLOSED_OPTION : (Integer) selectedValue);
        dialog.dispose();

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        List<Pair<String, String>> results = new ArrayList<>();
        for (Row row : rows) {
            results.add(row.getResult());
        }
        return results;
    }

    private static class Row extends JPanel {
        final QuestionTextField frontField;
        final QuestionTextField backField;

        Row(String frontLabel, String backLabel, int width, int height) {
            super(new GridBagLayout());
            frontField = new QuestionTextField(width, height);
            backField = new QuestionTextField(width, height);

            JScrollPane frontScroll = new JScrollPane(frontField);
            JScrollPane backScroll = new JScrollPane(backField);
            frontScroll.setPreferredSize(new Dimension(width, height / 6));
            backScroll.setPreferredSize(new Dimension(width, height / 12));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.weightx = 1;

            gbc.gridx = 0; gbc.gridy = 0; gbc.weighty = 0;
            add(new JLabel(frontLabel), gbc);

            gbc.gridy = 1; gbc.weighty = 0.5;
            gbc.fill = GridBagConstraints.BOTH;
            add(frontScroll, gbc);

            gbc.gridy = 2; gbc.weighty = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(new JLabel(backLabel), gbc);

            gbc.gridy = 3; gbc.weighty = 0.5;
            gbc.fill = GridBagConstraints.BOTH;
            add(backScroll, gbc);
        }

        Pair<String, String> getResult() {
            return new Pair<>(frontField.getText(), backField.getText());
        }
    }

    private static class PlusButton extends JButton {
        PlusButton() {
            super("+");
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("SansSerif", Font.BOLD, 20));
            setForeground(Color.WHITE);
            setBackground(new Color(60, 120, 220));
            setPreferredSize(new Dimension(36, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g2.setColor(getBackground().brighter());
            } else {
                g2.setColor(getBackground());
            }
            g2.fillOval(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }
}
