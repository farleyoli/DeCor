package fso.decor;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class DeleteCardDialog extends JDialog {
    private final int width = 1000;
    private final int height = 600;
    private JTable table;
    public DeleteCardDialog(Deck deck, Set<Card> cardsToShow, Book book) {
        super(new JFrame(), "Choose cards to delete", true);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JScrollPane tableSP = createTableFromCards(cardsToShow);
        add(tableSP);
        JButton b = new JButton("Delete Selected Cards");
        b.addActionListener(e -> {
            assert table != null;
            Set<Integer> pagesToRerender = new HashSet<>();
            for (int row : table.getSelectedRows()) {
                pagesToRerender.addAll(deck.deleteCard((Integer) table.getValueAt(row, 0)));
            }
            pagesToRerender.forEach(page -> book.getPageById(page).repaintScrollBar());
            DeleteCardDialog.this.setVisible(false);
        });
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(b);
        pack();
        setVisible(true);
    }

    private JScrollPane createTableFromCards(Set<Card> cards) {
        String[] columnNames = {"Card ID", "Front", "Back"};
        Object[][] data = new Object[cards.size()][3]; // id (int), front (String), back (String)
        int i = 0;
        for (Card card : cards) {
            data[i][0] = card.getId();
            data[i][1] = card.getFront();
            data[i][2] = card.getBack();
            i++;
        }
        JTable table = new JTable(data, columnNames);
        this.table = table;
        JScrollPane ret = new JScrollPane(table);
        ret.setPreferredSize(new Dimension(width, height));
        return ret;
    }
}
