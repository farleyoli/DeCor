package fso.decor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Page extends JPanel {
    private JLabel label;
    private ScrollBar bar;
    private int pageHeight;
    private int pageNumber;
    private final Book book;

    public Page(File pathToImage, Book book) {
        this.book = book;
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        try {
            BufferedImage img = ImageIO.read(pathToImage);
            ImageIcon icon = new ImageIcon(img);

//            int oW = icon.getIconWidth();
//            int oH = icon.getIconHeight();
//            // resizing
//            Image image = icon.getImage(); // transform it
//            Image newimg = image.getScaledInstance((int) (1.5 * oW), (int) (1.5 * oH),  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
//            icon = new ImageIcon(newimg);  // transform it back

            pageHeight = icon.getIconHeight();
            label = new JLabel(icon);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
        } catch (IOException e) {
            System.out.println(pathToImage.getAbsolutePath());
            e.printStackTrace();
            return;
        }
//        setBackground(Color.RED);
        bar = new ScrollBar(pageHeight, this);
        add(bar);
        add(label);

        label.addMouseListener(new PageMouseListener(this, book.getDeck(), book.getState()));
    }

    public Page(File pathToImage, int pageNumber, Book book) {
        this(pathToImage, book);
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void repaintScrollBar() {
        bar.repaint();
    }

    public Book getBook() {
        return book;
    }
}
