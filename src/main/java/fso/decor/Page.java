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
    private boolean isImageCreated;

    public Page(int pageNumber, Book book) {
        // don't create the image yet, as it takes time

        isImageCreated = false;
        this.isBlank = true;
        this.book = book;
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        this.pageNumber = pageNumber;
        PdfManager pdfManager = book.getPdfManager();
        var pair = pdfManager.getWidthAndHeight(pageNumber - 1);
        int width = pair.first;
        int height = pageHeight = pair.second;

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

        bar = new ScrollBar(pageHeight, this);
        add(bar);
        add(label);
        label.addMouseListener(new PageMouseListener(this, book.getDeck(), book.getState()));
    }

    private File createImage() {
        return book.getPdfManager().getPage(pageNumber - 1);
    }

    public void setIsBlank(boolean value) {
        isBlank = false;
    }

    public synchronized void showImage() {
        if (!isImageCreated) {
            pathToImage = createImage();
            isImageCreated = true;
        }

        if (!isBlank)
            return;

        try {
            BufferedImage img = ImageIO.read(pathToImage);
            ImageIcon icon = new ImageIcon(img);
            remove(label);
            remove(label);
            label = new JLabel(icon) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    paintSelectionOverlay(g);
                }
            };
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

    // TODO: Get this shared code into some external function
    public void hideImage() {
        try {
            BufferedImage img = ImageIO.read(pathToImage);
            remove(label);
            remove(label);
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
        add(label);
        label.addMouseListener(new PageMouseListener(this, book.getDeck(), book.getState()));
        this.isBlank = true;
    }

    private void paintSelectionOverlay(Graphics g) {
        BookState bookState = book.getState();
        if (bookState.getState() == State.WAIT_IMAGE_END && bookState.getImageStartPage() == pageNumber) {
            Graphics2D g2d = (Graphics2D) g;
            int x = bookState.getImageStartX();
            int y = bookState.getImageStartY();

            // Draw crosshair at first click position
            g2d.setColor(new Color(0, 120, 215, 200));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x - 20, y, x + 20, y);
            g2d.drawLine(x, y - 20, x, y + 20);
            g2d.drawOval(x - 10, y - 10, 20, 20);
        }
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

    public void repaintLabel() {
        label.repaint();
    }

    public Book getBook() {
        return book;
    }
}
