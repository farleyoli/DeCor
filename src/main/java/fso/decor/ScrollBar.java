package fso.decor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ScrollBar extends JPanel {
    private final int imageHeight;
    private final Page page;

    private final int width = 20;
    public ScrollBar(int height, Page page) {
        this.imageHeight = height;
        this.page = page;
        setMinimumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D graphic2d = (Graphics2D) g;
        graphic2d.setColor(Color.GRAY);
        graphic2d.fillRect(0, 0, width, imageHeight);
        ArrayList<Double> cardPositions = page.getBook().getDeck().renderScrollBar(page.getPageNumber());
        for (int i = 0; i < cardPositions.size(); i += 2) {
            graphic2d.setColor(Color.BLUE);
            graphic2d.fillRect(0, (int) (cardPositions.get(i) * imageHeight), width,
                    (int) ((cardPositions.get(i+1) - cardPositions.get(i)) * imageHeight));
        }
    }
}
