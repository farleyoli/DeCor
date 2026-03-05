package fso.decor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainContainer extends JFrame {
    final private Book book;
    final private File destFolder;
    final private File source;
    final private JScrollPane scrollPane;
    final private PdfManager pdfManager;

    final private int scrollPaneMouseIncrement = 18;
    private String pdfName = "";
    private int maxPositionOfPage;
    private JLabel ankiStatusLabel;

    public MainContainer() {
        this(null);
    }

    public MainContainer(String pdfNameParam) {  // null indicates we have to pick it
        setupMenuBar();

        createDirectories();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (pdfNameParam == null)
            pdfName = fetchPdfName(false);
        else
            pdfName = pdfNameParam;

        source = getPdfFile();

        destFolder = GlobalConfig.getImageFolder();

        pdfManager = new PdfManager(source, destFolder);

        String hash = pdfManager.getPdfHash();
        GlobalConfig config = GlobalConfig.getInstance(hash);
        config.setPdfName(pdfName);

        book = new Book(hash);
        book.setLayout(new BoxLayout(book, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(book);
        scrollPane.getVerticalScrollBar().setUnitIncrement(scrollPaneMouseIncrement);
        getContentPane().add(scrollPane);

        setupPanel();

        setKeybindings();

        // TODO: set this in the config
        setTitle("DeCor - " + pdfName);
        setSize(1366, 768);
        setVisible(true);
        addListenersForRendering();
        scrollToLastMarkedPage();
        getPageToPosition();

        updateAnkiStatus();
        Timer ankiTimer = new Timer(10000, e -> updateAnkiStatus());
        ankiTimer.start();
    }

    private File getPdfFile() {
        return getPdfFile(true);
    }

    private File getPdfFile(boolean showDialog) {
        File dest = new File(GlobalConfig.getPdfFolder(), pdfName + ".pdf");
        if (dest.exists())
            return dest;

        if (showDialog)
            JOptionPane.showMessageDialog(null, "Please select a PDF file.");
        // choose a pdf file from somewhere else
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Files", "pdf");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            String filepath = fileChooser.getSelectedFile().getAbsolutePath();
            File source = new File(filepath);
            pdfName = fileChooser.getSelectedFile().getName().replace(".pdf", "");
            dest = new File(GlobalConfig.getPdfFolder(), pdfName + ".pdf");
            try {
                Files.copy(source.toPath(), dest.toPath(),StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return dest;
        }

        System.err.println("No valid pdf file was provided for the program to work with");
        System.exit(1);

        return null;
    }

    private void createDirectories() {
        File linuxDirectory = GlobalConfig.getBaseFolder();
        if (!linuxDirectory.exists())
            linuxDirectory.mkdirs();
        File pdfDirectory = GlobalConfig.getPdfFolder();
        if (!pdfDirectory.exists()) {
            pdfDirectory.mkdirs();
        }
        File imageDirectory = GlobalConfig.getImageFolder();
        if (!imageDirectory.exists()) {
            imageDirectory.mkdirs();
        }
    }

    private void scrollToLastMarkedPage() {
        Page lastMarkedPage = book.getLastMarkedPage();
        if (lastMarkedPage == null) // We need this for when no page is marked
            return;
        JViewport view = scrollPane.getViewport();
        Point p = lastMarkedPage.getLocation();
        view.setViewPosition(p);
    }

    private Map<Integer, Integer> getPageToPosition() {
        Map<Integer, Integer> ret = new HashMap<>();
        int maxPositionOfPage = -1;
        for (int id : book.getIdsSet()) {
            int pageStep = GlobalConfig.getPageStep();
            int pos = (int) Math.round(book.getPageById(id).getLocation().getY() / pageStep) * pageStep;
            ret.put(pos, id);
            if (pos > maxPositionOfPage)
                maxPositionOfPage = pos;
        }
        this.maxPositionOfPage = maxPositionOfPage;
        return ret;
    }

    // TODO: verify if there isn't a better kind of event to use here
    private void addListenersForRendering() {
        int pageStep = GlobalConfig.getPageStep();
        int imageFileBufferSize = GlobalConfig.getImageFileBufferSize();
        Map<Integer, Integer> positions = getPageToPosition();
        JViewport view = scrollPane.getViewport();
        view.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int position = Math.round(view.getViewPosition().y / pageStep) * pageStep;

                // TODO: this looks okay now, but check that the multi-threading logic is working well better later

                // show pages
                for (int i = position - imageFileBufferSize * pageStep;
                     i <= maxPositionOfPage && i <= position + imageFileBufferSize * pageStep;
                     i += pageStep)
                {
                    if (positions.containsKey(i)) {
                        Page p = book.getPageById(positions.get(i));
                        // System.out.println("Before check");  // DEBUG
                        if (p != null && p.isBlank()) {
                            // if I don't spawn new threads, scrolling is blocked when these are running
                            new Thread(() -> {
                                // System.out.println("After check");  // DEBUG
                                p.showImage();
                            }).start();
                        }
                    }
                }
                
                // hide pages after
                for (int i = position + (imageFileBufferSize + 1) * pageStep;
                     i <= maxPositionOfPage && i <= position + (3 * imageFileBufferSize + 1) * pageStep;
                     i += pageStep)
                {
                    if (positions.containsKey(i)) {
                        Page p = book.getPageById(positions.get(i));
                        if (p != null && !p.isBlank()) {
                            p.hideImage();
                        }
                    }
                }

                // hide pages before
                for (int i = position - (imageFileBufferSize + 1) * pageStep;
                     i >= 0 && i >= position - (3 * imageFileBufferSize + 1) * pageStep;
                     i -= pageStep)
                {
                    if (positions.containsKey(i)) {
                        Page p = book.getPageById(positions.get(i));
                        if (p != null && !p.isBlank()) {
                            p.hideImage();
                        }
                    }
                }
            }
        });
    }

    private String fetchPdfName(boolean forceOpen) {
        if (forceOpen) {
            getPdfFile(false);
            return pdfName;
        }
        File pdfDir = GlobalConfig.getPdfFolder();
        ArrayList<String> choicesList = new ArrayList<>();
        for (final File fileEntry : pdfDir.listFiles()) {
            if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".pdf"))
                choicesList.add(fileEntry.getName().substring(0, fileEntry.getName().length() - 4));
        }
        if (choicesList.size() == 0) {
            getPdfFile();
            return pdfName;
        }
        String[] choices = new String[choicesList.size()];
        choicesList.toArray(choices);
        return (String) JOptionPane.showInputDialog(null, "Choose pdf to open",
                "Choose pdf to open", JOptionPane.QUESTION_MESSAGE, null,
                choices, // Array of choices
                choices[0]); // Initial choice
    }

    private void setupPanel() {
        int total = pdfManager.getNumberOfPages();
        for (int pageNumber = 0; pageNumber < total; pageNumber++) {
            book.addBlankPage(pdfManager);
        }
    }

    private void setKeybindings() {
        setActions();
        InputMap im = scrollPane.getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0), "move unit down");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0), "move unit up");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "move down");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "move up");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "delete");
        im.put(KeyStroke.getKeyStroke("control S"), "save deck");
        im.put(KeyStroke.getKeyStroke("control A"), "sync anki");
    }

    private void setActions() {
        scrollPane.getActionMap().put("move unit down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getValue() + 50);
            }
        });

        scrollPane.getActionMap().put("move unit up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getValue() - 50);
            }
        });

        scrollPane.getActionMap().put("move down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getValue() + 200);
            }
        });

        scrollPane.getActionMap().put("move up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getValue() - 200);
            }
        });

        scrollPane.getActionMap().put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                book.remove(3);
                book.revalidate();
                book.repaint();
            }
        });
        scrollPane.getActionMap().put("save deck", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDeck();
            }
        });
        scrollPane.getActionMap().put("sync anki", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAndSyncAnki();
            }
        });
    }

    // TODO: don't throw here, show a dialog or something 
    private void saveDeck() {
        File fileToSave = new File(GlobalConfig.getImageFolder(), pdfManager.getPdfHash() + ".deck");
        try (PrintWriter out = new PrintWriter(fileToSave)) {
            out.print(book.getDeck().getSerialiseString());
            JOptionPane.showMessageDialog(null, "Saved file successfully!");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void saveAndSyncAnki() {
        AnkiConnectHandler handler = AnkiConnectHandler.getInstance(pdfManager.getPdfHash());

        if (!handler.isConnected()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to Anki.\nMake sure Anki is running and the AnkiConnect plugin is installed.",
                    "Anki not connected", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int dialogResult = JOptionPane.showConfirmDialog(null,
                "Would you like to Sync with Anki?");
        if (dialogResult != JOptionPane.YES_OPTION) {
            return;
        }

        File fileToSave = new File(GlobalConfig.getImageFolder(), pdfManager.getPdfHash() + ".deck");
        try (PrintWriter out = new PrintWriter(fileToSave)) {
            out.print(book.getDeck().getSerialiseString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        String modelName = GlobalConfig.getModelName();
        handler.createModelIfAbsent(modelName);
        handler.createDeckIfAbsent(GlobalConfig.getDeckName(pdfName));
        handler.transferMedia(book.getIdsToAdd());
        for (Card card : book.getDeck().getCards()) {
            if (!card.isNew())
                continue;

            card.setNew(false);
            handler.addCard(card.getAnkiRequest(modelName));
        }
        JOptionPane.showMessageDialog(null,
                "Synced with Anki and saved file successfully!");
    }

    private void updateAnkiStatus() {
        new Thread(() -> {
            boolean connected = AnkiConnectHandler.getInstance(pdfManager != null ?
                    pdfManager.getPdfHash() : "").isConnected();
            SwingUtilities.invokeLater(() -> {
                if (connected) {
                    ankiStatusLabel.setText("Anki: Connected");
                    ankiStatusLabel.setForeground(new Color(0, 128, 0));
                } else {
                    ankiStatusLabel.setText("Anki: Not connected");
                    ankiStatusLabel.setForeground(Color.RED);
                }
            });
        }).start();
    }

    private void deletePdfAndData() {
        int result = JOptionPane.showConfirmDialog(this,
                "Delete \"" + pdfName + "\" and all its images and deck data?\nThis cannot be undone.",
                "Delete PDF and data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.YES_OPTION)
            return;

        String hash = pdfManager.getPdfHash();

        // Delete the PDF file
        File pdfFile = new File(GlobalConfig.getPdfFolder(), pdfName + ".pdf");
        if (pdfFile.exists())
            pdfFile.delete();

        // Delete image files and deck file
        File imageFolder = GlobalConfig.getImageFolder();
        File[] files = imageFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(hash)) {
                    file.delete();
                }
            }
        }

        // Reopen with PDF picker
        dispose();
        EventQueue.invokeLater(() -> {
            new MainContainer();
        });
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem deleteItem = new JMenuItem("Delete PDF and data");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(openItem);
        fileMenu.add(deleteItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Action");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAndSyncItem = new JMenuItem("Save and sync with Anki");
        editMenu.add(saveItem);
        editMenu.add(saveAndSyncItem);

        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem opacityItem = new JMenuItem("Dialog transparency...");
        settingsMenu.add(opacityItem);

        opacityItem.addActionListener(e -> {
            int currentPercent = Math.round((1f - GlobalConfig.getDialogOpacity()) * 100);
            JSlider slider = new JSlider(0, 90, currentPercent);
            slider.setMajorTickSpacing(10);
            slider.setMinorTickSpacing(5);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            int result = JOptionPane.showConfirmDialog(this, slider,
                    "Dialog transparency (%)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                float opacity = 1f - slider.getValue() / 100f;
                GlobalConfig.setDialogOpacity(opacity);
            }
        });

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(settingsMenu);

        menuBar.add(Box.createHorizontalGlue());
        ankiStatusLabel = new JLabel();
        ankiStatusLabel.setFont(ankiStatusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        ankiStatusLabel.setText("Anki: Checking...");
        ankiStatusLabel.setForeground(Color.GRAY);
        menuBar.add(ankiStatusLabel);
        menuBar.add(Box.createHorizontalStrut(10));

        openItem.addActionListener(e -> {
            dispose(); // Close/delete the current JFrame
            EventQueue.invokeLater(() -> {
                pdfName = "";
                getPdfFile(false);
                MainContainer newMainContainer = new MainContainer(pdfName);
                newMainContainer.setVisible(true);
            });
        });

        deleteItem.addActionListener(e -> {
            deletePdfAndData();
        });

        exitItem.addActionListener(e -> {
            System.exit(0);
        });

        saveItem.addActionListener(e -> {
            saveDeck();
        });

        saveAndSyncItem.addActionListener(e -> {
            saveAndSyncAnki();
        });

        setJMenuBar(menuBar);
    }

}
