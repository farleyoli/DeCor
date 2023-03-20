package fso.decor;

import javax.swing.*;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;

public class Book extends JPanel {
    private int lastPage;
    private final Deck deck;
    private final BookState state;
    private final HashMap<Integer, Page> idToPage;
    private final String pdfHash;

    public Book(String pdfHash) {
        this.pdfHash = pdfHash;
        deck = new Deck(pdfHash);
        lastPage = 1;
        state = new BookState();
        idToPage = new HashMap<>();
    }
    public void addPage(File pathToImage) {
        Page page = new Page(pathToImage, lastPage, this);
        idToPage.put(lastPage, page);
        lastPage++;
        add(page);
    }

    public void addBlankPage(File pathToImage) {
        Page page = new Page(pathToImage, lastPage, this);
        idToPage.put(lastPage, page);
        lastPage++;
        add(page);
        if (lastPage < 10)
            page.showImage();
    }

    public Deck getDeck() {
        return deck;
    }

    public BookState getState() {
        return state;
    }

    public Page getPageById(int id) {
        return idToPage.get(id);
    }

    public Page getLastMarkedPage() {
        int lastMarkedPage = Collections.max(deck.getPageToCards().keySet());
        return idToPage.get(lastMarkedPage);
    }
}
