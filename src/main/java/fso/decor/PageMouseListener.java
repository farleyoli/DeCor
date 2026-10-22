package fso.decor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        if (SwingUtilities.isRightMouseButton(e)) {
            handleRightClick(e);
            return;
        }
        if (e.isControlDown()) {
            mouseClickedWithControlDown(e);
            return;
        }
        if (state.getState() == State.WAIT_IMAGE_END) {
            // Ignore left-clicks during image selection
            return;
        }
        mouseClickedWithoutAdditionalKeys(e);
    }

    private void handleRightClick(MouseEvent e) {
        switch (state.getState()) {
            case READY -> {
                state.startImageSelection(e.getX(), e.getY(), page.getPageNumber());
                page.repaintLabel();
            }
            case WAIT_IMAGE_END -> {
                int startPageNum = state.getImageStartPage();
                int endPageNum = page.getPageNumber();

                // Limit to same page or adjacent pages
                if (Math.abs(endPageNum - startPageNum) > 1) {
                    state.cancelImageSelection();
                    Page startPage = page.getBook().getPageById(startPageNum);
                    if (startPage != null) startPage.repaintLabel();
                    return;
                }

                state.finishImageSelection();

                try {
                    File imageFile = cropAndSaveImage(e.getX(), e.getY(), endPageNum);
                    if (imageFile != null) {
                        state.setImageQuestionFile(imageFile);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Repaint start page to clear crosshair
                Page startPage = page.getBook().getPageById(startPageNum);
                if (startPage != null) startPage.repaintLabel();
                if (endPageNum != startPageNum) {
                    page.repaintLabel();
                }
            }
            case WAIT_CARD_END -> {
                // Ignore right-clicks during card region selection
            }
        }
    }

    private File cropAndSaveImage(int endX, int endY, int endPageNum) throws IOException {
        int startX = state.getImageStartX();
        int startY = state.getImageStartY();
        int startPageNum = state.getImageStartPage();

        // Normalize page order
        if (endPageNum < startPageNum) {
            int tmp = startPageNum; startPageNum = endPageNum; endPageNum = tmp;
            int tmpX = startX; startX = endX; endX = tmpX;
            int tmpY = startY; startY = endY; endY = tmpY;
        }

        int left = Math.min(startX, endX);
        int right = Math.max(startX, endX);
        int width = right - left;

        if (width < 10) return null; // Too small, treat as cancel

        PdfManager pdfManager = page.getBook().getPdfManager();
        String hash = pdfManager.getPdfHash();

        if (startPageNum == endPageNum) {
            // Single page crop
            int top = Math.min(startY, endY);
            int bottom = Math.max(startY, endY);
            int height = bottom - top;
            if (height < 10) return null;

            File pageFile = pdfManager.getPage(startPageNum - 1);
            BufferedImage pageImg = ImageIO.read(pageFile);

            // Clamp to image bounds
            left = Math.max(0, left);
            top = Math.max(0, top);
            right = Math.min(pageImg.getWidth(), right);
            bottom = Math.min(pageImg.getHeight(), bottom);
            width = right - left;
            height = bottom - top;
            if (width < 10 || height < 10) return null;

            BufferedImage cropped = pageImg.getSubimage(left, top, width, height);

            String filename = hash + "_q_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(GlobalConfig.getImageFolder(), filename);
            ImageIO.write(cropped, "jpg", outputFile);
            return outputFile;
        } else {
            // Cross-page crop (adjacent pages)
            File pageFile1 = pdfManager.getPage(startPageNum - 1);
            File pageFile2 = pdfManager.getPage(endPageNum - 1);
            BufferedImage img1 = ImageIO.read(pageFile1);
            BufferedImage img2 = ImageIO.read(pageFile2);

            // Clamp horizontal bounds
            left = Math.max(0, left);
            right = Math.min(Math.min(img1.getWidth(), img2.getWidth()), right);
            width = right - left;
            if (width < 10) return null;

            int cropTop1 = Math.max(0, startY);
            int cropBottom1 = img1.getHeight();
            int cropTop2 = 0;
            int cropBottom2 = Math.min(img2.getHeight(), endY);

            int h1 = cropBottom1 - cropTop1;
            int h2 = cropBottom2 - cropTop2;
            if (h1 + h2 < 10) return null;

            BufferedImage cropped1 = img1.getSubimage(left, cropTop1, width, h1);
            BufferedImage cropped2 = img2.getSubimage(left, cropTop2, width, h2);

            // Stitch vertically
            BufferedImage stitched = new BufferedImage(width, h1 + h2, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = stitched.createGraphics();
            g.drawImage(cropped1, 0, 0, null);
            g.drawImage(cropped2, 0, h1, null);
            g.dispose();

            String filename = hash + "_q_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(GlobalConfig.getImageFolder(), filename);
            ImageIO.write(stitched, "jpg", outputFile);
            return outputFile;
        }
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
                int startPage   = state.getPage();
                int endPage     = page.getPageNumber();
                double start    = state.setCardEnd();
                double end      = getPercentagePosition(e);

                File questionImageFile = null;
                List<Pair<String, String>> results;

                if (state.hasImageQuestion()) {
                    questionImageFile = state.getImageQuestionFile();
                    results = CardCreationDialog.showImageQuestionDialog(questionImageFile);
                    if (results == null) return; // Dialog canceled, image question stays pending
                } else {
                    results = CardCreationDialog.showQuestionDialog();
                    if (results == null) return;
                }

                // Ensure that start is before end
                if (endPage < startPage || endPage == startPage && end < start) {
                    int intTemp = startPage;
                    startPage = endPage;
                    endPage = intTemp;
                    double doubleTemp = start;
                    start = end;
                    end = doubleTemp;
                }

                String shortName = null;
                if (questionImageFile != null) {
                    String name = questionImageFile.getName();
                    shortName = name.length() > 50 ? name.substring(name.length() - 50) : name;
                }

                List<Card> newCards = new ArrayList<>();
                for (Pair<String, String> result : results) {
                    String questionText = result.first == null ? "" : result.first.strip();
                    String extra = result.second == null ? "" : result.second.strip();

                    if (questionImageFile != null) {
                        if (questionText.isEmpty() && extra.isEmpty()) continue;
                    } else {
                        if (questionText.isEmpty()) continue;
                    }

                    String question;
                    if (questionImageFile != null) {
                        question = "<img src=\"" + shortName + "\">";
                        if (!questionText.isEmpty()) {
                            question = questionText + "<br>" + question;
                        }
                    } else {
                        question = questionText;
                    }

                    newCards.add(deck.addCard(question, extra, startPage, start, endPage, end));
                }

                if (newCards.isEmpty())
                    return;

                if (questionImageFile != null) {
                    state.clearImageQuestion();
                }

                Book book = page.getBook();
                for (int id = startPage; id <= endPage; id++) {
                    Page page = book.getPageById(id);
                    page.repaintScrollBar();
                }

                syncCardsDirectly(newCards, book, questionImageFile);
            }
            default -> throw new IllegalStateException("Unexpected value: " + state.getState());
        }
    }

    private void syncCardsDirectly(List<Card> cards, Book book, File questionImageFile) {
        String hash = book.getPdfManager().getPdfHash();
        saveDeck(hash);
        new Thread(() -> {
            AnkiConnectHandler handler = AnkiConnectHandler.getInstance(hash);
            if (!handler.isConnected())
                return;

            String modelName = GlobalConfig.getModelName();
            GlobalConfig config = GlobalConfig.getInstance(hash);
            handler.createModelIfAbsent(modelName);
            handler.createDeckIfAbsent(config.getDeckName());
            handler.transferMedia(book.getIdsToAdd());
            if (questionImageFile != null) {
                handler.transferSingleFile(questionImageFile);
            }
            for (Card card : cards) {
                handler.addCard(card.getAnkiRequest(modelName));
                card.setNew(false);
            }
        }).start();
    }

    private void saveDeck(String hash) {
        File fileToSave = new File(GlobalConfig.getImageFolder(), hash + ".deck");
        try (PrintWriter out = new PrintWriter(fileToSave)) {
            out.print(deck.getSerialiseString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
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
