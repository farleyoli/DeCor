package fso.decor;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Book extends JPanel {
    private int lastPage;
    private final Deck deck;
    private final BookState state;
    private final HashMap<Integer, Page> idToPage;
    private PdfManager pdfManager;

    public Book(String pdfHash) {
        deck = new Deck(pdfHash);
        lastPage = 1;
        state = new BookState();
        idToPage = new HashMap<>();
    }

    public PdfManager getPdfManager() {
        return pdfManager;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void addBlankPage(int startPage, PdfManager pdfManager) {
        this.pdfManager = pdfManager;
        Page page = new Page(lastPage, this);
        idToPage.put(lastPage, page);
        lastPage++;
        add(page);
        if (startPage - 6 < lastPage && lastPage < startPage + 6)
            page.showImage();
    }

    public void addBlankPage(PdfManager pdfManager) {
        addBlankPage(1, pdfManager);
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
        if (deck.getPageToCards().size() > 0) {
            int lastMarkedPage = Collections.max(deck.getPageToCards().keySet());
            return idToPage.get(lastMarkedPage);
        }
        return null;
    }

    public Set<Integer> getIdsToAdd() {
        Set<Integer> ret = new HashSet<Integer>();
        for (int id : deck.getPageToCards().keySet()) {
            for (int i = id - 5; i <= id + 5; i++) {
                if (i < 0)
                    continue;
                ret.add(i);
            }
        }
        return ret;
    }

    public Set<Integer> getIdsSet() {
        return idToPage.keySet();
    }
}
