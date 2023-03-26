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
    private boolean isBlank;
    private File pathToImage;

    public Page(File pathToImage, int pageNumber, Book book) {
        this.isBlank = true;
        this.book = book;
        this.pathToImage = pathToImage;
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        this.pageNumber = pageNumber;
        try {
            BufferedImage img = ImageIO.read(pathToImage);
            int width = img.getWidth();
            int height = pageHeight = img.getHeight();
            label = new JLabel(){
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(0, 0, width, height);
                }
            };
            label.setPreferredSize(new Dimension(width, height));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setBackground(Color.GREEN);
        } catch (IOException e) {
            System.out.println(pathToImage.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        bar = new ScrollBar(pageHeight, this);
        add(bar);
        add(label);
        label.addMouseListener(new PageMouseListener(this, book.getDeck(), book.getState()));
        this.pageNumber = pageNumber;
    }

    public void showImage() {
        try {
            BufferedImage img = ImageIO.read(pathToImage);
            ImageIcon icon = new ImageIcon(img);
            remove(label);
            label = new JLabel(icon);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
        } catch (IOException e) {
            System.out.println(pathToImage.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        add(label);
        label.addMouseListener(new PageMouseListener(this, book.getDeck(), book.getState()));
        this.isBlank = false;
    }

    public boolean isBlank() {
        return isBlank;
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
