package fso.decor;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;

public class PageMouseListener implements MouseListener {
    private final Page page;
    private final Deck deck;

    private BookState state;
    public PageMouseListener(Page page, Deck deck) {
        this.page = page;
        this.deck = deck;
    }

    public PageMouseListener(Page page, Deck deck, BookState state) {
        this(page, deck);
        this.state = state;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isControlDown()) {
            mouseClickedWithControlDown(e);
            return;
        }
        mouseClickedWithoutAdditionalKeys(e);
    }

    private void mouseClickedWithControlDown(MouseEvent e) {
        int pageNumber = page.getPageNumber();
        double position = getPercentagePosition(e);
        HashSet<Card> cards = (HashSet<Card>) deck.getCardsInPosition(pageNumber, position);
        if (!cards.isEmpty()) {
            new DeleteCardDialog(deck, cards, page.getBook());
        }
    }

    private void mouseClickedWithoutAdditionalKeys(MouseEvent e) {
        switch (state.getState()) {
            case READY -> state.setCardStart(getPercentagePosition(e), page.getPageNumber());
            case WAIT_CARD_END -> {
                int initialPage = state.getPage();
                int endPage = page.getPageNumber();
                double start = state.setCardEnd();
                double end = getPercentagePosition(e);
                JFrame frame = new JFrame();
                String input = JOptionPane.showInputDialog(frame, "Input:");
                String output = "";//JOptionPane.showInputDialog(frame, "Output:");
                if (input == null) input = "";
                if (output == null) output = "";
                deck.addCard(input, output, initialPage, start, endPage, end);
                Book book = page.getBook();
                for (int id = initialPage; id <= endPage; id++) {
                    Page page = book.getPageById(id);
                    page.repaintScrollBar();
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + state.getState());
        }
    }

    private double getPercentagePosition(MouseEvent e) {
        int pagePosition = e.getY();
        return (double) pagePosition / page.getPageHeight();
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
}
