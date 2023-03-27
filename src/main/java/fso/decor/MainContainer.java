package fso.decor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainContainer extends JFrame {
    final private Book book;
    final private File destFolder;
    final private File source;
    final private JScrollPane scrollPane;
    final private PdfManager pdfManager;

    final private int scrollPaneMouseIncrement = 18;
    private final String pdfName;
    private int maxPositionOfPage;

    private void createDirectories() {
        File linuxDirectory = new File(GlobalConfig.getBaseFolder());
        if (!linuxDirectory.exists())
            linuxDirectory.mkdirs();
        File pdfDirectory = new File(GlobalConfig.getPdfFolder());
        if (!pdfDirectory.exists()) {
            pdfDirectory.mkdirs();
        }
        File imageDirectory = new File(GlobalConfig.getImageFolder());
        if (!imageDirectory.exists()) {
            imageDirectory.mkdirs();
        }
    }

    public MainContainer() {
        createDirectories();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pdfName = fetchPdfName();

        source = new File(GlobalConfig.getPdfFolder() + pdfName + ".pdf");
        destFolder = new File(GlobalConfig.getImageFolder());
        pdfManager = new PdfManager(source, destFolder, true);
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

        setSize(800, 600);
        setVisible(true);
        addListenersForRendering();
        scrollToLastMarkedPage();
        getPageToPosition();
    }

    private void scrollToLastMarkedPage() {
        Page lastMarkedPage = book.getLastMarkedPage();
        if (lastMarkedPage == null) // We need this for when no page is marked
            return;
        JViewport view = scrollPane.getViewport();
        Point p = lastMarkedPage.getLocation();
        view.setViewPosition(p);
    }

    // TODO: Make sure we do not accept image heights smaller than 100
    // TODO: Get rid of magic number
    private Map<Integer, Integer> getPageToPosition() {
        Map<Integer, Integer> ret = new HashMap<>(); // pages will be at least 10 separated
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
        Map<Integer, Integer> positions = getPageToPosition();
        JViewport view = scrollPane.getViewport();
        view.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int position = (int) Math.round(view.getViewPosition().y / pageStep) * pageStep;

                // show pages
                for (int i = position - 10 * pageStep; i <= maxPositionOfPage && i <= position + 10 * pageStep; i += pageStep) {
                    if (positions.containsKey(i)) {
                        Page p = book.getPageById(positions.get(i));
                        if (p != null && p.isBlank()) {
                            p.showImage();
                        }
                    }
                }
                
                // hide pages
                for (int i = position + 11 * pageStep; i <= maxPositionOfPage && i <= position + 31 * pageStep; i += pageStep) {
                    if (positions.containsKey(i)) {
                        Page p = book.getPageById(positions.get(i));
                        if (p != null && !p.isBlank()) {
                            p.hideImage();
                        }
                    }
                }
                for (int i = position - 11 * pageStep; i >= 0 && i >= position - 31 * pageStep; i -= pageStep) {
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


    private String fetchPdfName() {
        File pdfDir = new File(GlobalConfig.getPdfFolder());
        ArrayList<String> choicesList = new ArrayList<>();
        for (final File fileEntry : pdfDir.listFiles()) {
            if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".pdf"))
                choicesList.add(fileEntry.getName().substring(0, fileEntry.getName().length() - 4));
        }
        String[] choices = new String[choicesList.size()];
        choicesList.toArray(choices);
        return (String) JOptionPane.showInputDialog(null, "Choose pdf to open",
                "Choose pdf to open", JOptionPane.QUESTION_MESSAGE, null, // Use
                // default
                // icon
                choices, // Array of choices
                choices[0]); // Initial choice
    }

    private void setupPanel() {
        String pdfHash = pdfManager.getPdfHash();
        File[] resources = destFolder.listFiles();
        if (resources == null)
            return;
        Arrays.sort(resources);
        for (File file : resources) {
            if (file.getName().contains(pdfHash) && file.getName().endsWith(".jpg")) {
                book.addBlankPage(file);
            }
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
                File fileToSave = new File("src/main/resources/" + pdfManager.getPdfHash() + ".deck");
                try (PrintWriter out = new PrintWriter(fileToSave)) {
                    out.print(book.getDeck().getSerialiseString());
                    JOptionPane.showMessageDialog(null, "Saved file successfully!");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        scrollPane.getActionMap().put("sync anki", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogResult = JOptionPane.showConfirmDialog(null,
                        "Would you like to Sync with Anki?");
                if (dialogResult != JOptionPane.YES_OPTION) {
                    return;
                }
                AnkiConnectHandler handler = AnkiConnectHandler.getInstance(pdfManager.getPdfHash());
                handler.createDeckIfAbsent("DeCor::" + pdfName);
                handler.transferMedia(book.getIdsToAdd());
                for (Card card : book.getDeck().getCards()) {
                    handler.addCard(card.getAnkiRequest());
                }
                File fileToSave = new File(GlobalConfig.getImageFolder() + pdfManager.getPdfHash() + ".deck");
                try (PrintWriter out = new PrintWriter(fileToSave)) {
                    out.print(book.getDeck().getSerialiseString());
                    JOptionPane.showMessageDialog(null,
                            "Synced with Anki and saved file successfully!");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
