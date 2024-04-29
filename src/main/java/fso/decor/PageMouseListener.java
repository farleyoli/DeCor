package fso.decor;

import javax.swing.*;
import java.awt.*;
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

    private Pair<String, String> getQuestionDialogResult() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width / 3;
        int height = screenSize.height / 3;

        QuestionTextField questionField = new QuestionTextField(width, height);
        QuestionTextField extraField = new QuestionTextField(width, height);

        JScrollPane questionScrollPane = new JScrollPane(questionField);
        JScrollPane extraScrollPane = new JScrollPane(extraField);

        questionScrollPane.setPreferredSize(new Dimension(width, height * 2 / 3));
        extraScrollPane.setPreferredSize(new Dimension(width, height / 3));

        JPanel panel = new JPanel(new GridBagLayout());

        JLabel questionLabel = new JLabel("Question:");
        JLabel extraLabel = new JLabel("Extra:");

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridwidth = GridBagConstraints.REMAINDER; // End row
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1;

        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.weighty = 0; 
        panel.add(questionLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.66;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(questionScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(extraLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0.33; 
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(extraScrollPane, gbc);

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

        JDialog dialog = pane.createDialog(null, "Create Card");

        // focus on first text field
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent e) {
                questionField.requestFocus();
            }
        });

        dialog.setVisible(true);
        
        Object selectedValue = pane.getValue();

        int result = (selectedValue == null ? JOptionPane.CLOSED_OPTION : (Integer) selectedValue);

        dialog.dispose();
        
        String question = "";
        String extra = "";
        if (result == JOptionPane.OK_OPTION) {
            question = questionField.getText();
            extra = extraField.getText();
        }

        return new Pair<String, String>(question, extra);
    }

    private void mouseClickedWithoutAdditionalKeys(MouseEvent e) {
        switch (state.getState()) {
            case READY -> state.setCardStart(getPercentagePosition(e), page.getPageNumber());
            case WAIT_CARD_END -> {
                int startPage   = state.getPage();
                int endPage     = page.getPageNumber();
                double start    = state.setCardEnd();
                double end      = getPercentagePosition(e);

                var res = getQuestionDialogResult();
                String question = res.first;
                String extra = res.second;

                if (question == null || question.length() == 0)
                    return;

                // Ensure that start is start is before end
                // not doing this in a separate function because Java is a horrible language 
                if (endPage < startPage || endPage == startPage && end < start) {
                    int intTemp = startPage;
                    startPage = endPage;
                    endPage = intTemp;
                    double doubleTemp = start;
                    start = end;
                    end = doubleTemp;
                }

                deck.addCard(question, extra, startPage, start, endPage, end);

                Book book = page.getBook();
                for (int id = startPage; id <= endPage; id++) {
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
