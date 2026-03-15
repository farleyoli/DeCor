package fso.decor;

import java.io.File;

enum State {
    READY,
    WAIT_CARD_END,
    WAIT_IMAGE_END,
}

public class BookState {
    private State state;
    private double cardStart;
    private int page;

    // Image question selection
    private int imageStartX, imageStartY, imageStartPage;
    private File imageQuestionFile;
    private boolean hasImageQuestion;

    public BookState() {
        this.state = State.READY;
        this.cardStart = 0;
        this.hasImageQuestion = false;
    }

    public State getState() {
        return state;
    }

    public void setCardStart(double cardStart, int page) {
        assert this.state == State.READY;
        this.cardStart = cardStart;
        this.state = State.WAIT_CARD_END;
        this.page = page;
    }

    public double setCardEnd() {
        assert this.state == State.WAIT_CARD_END;
        this.state = State.READY;
        double ret = this.cardStart;
        this.cardStart = 0;
        return ret;
    }

    public int getPage() {
        return page;
    }

    // Image selection methods
    public void startImageSelection(int x, int y, int page) {
        this.imageStartX = x;
        this.imageStartY = y;
        this.imageStartPage = page;
        this.state = State.WAIT_IMAGE_END;
    }

    public void finishImageSelection() {
        assert this.state == State.WAIT_IMAGE_END;
        this.state = State.READY;
    }

    public void cancelImageSelection() {
        if (this.state == State.WAIT_IMAGE_END) {
            this.state = State.READY;
        }
    }

    public int getImageStartX() { return imageStartX; }
    public int getImageStartY() { return imageStartY; }
    public int getImageStartPage() { return imageStartPage; }

    public void setImageQuestionFile(File file) {
        this.imageQuestionFile = file;
        this.hasImageQuestion = true;
    }

    public File getImageQuestionFile() {
        return imageQuestionFile;
    }

    public boolean hasImageQuestion() {
        return hasImageQuestion;
    }

    public void clearImageQuestion() {
        this.imageQuestionFile = null;
        this.hasImageQuestion = false;
    }
}
