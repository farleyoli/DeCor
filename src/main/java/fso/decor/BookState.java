package fso.decor;

enum State {
    READY,
    WAIT_CARD_END,
}

public class BookState {
    private State state;
    private double cardStart;

    private int page;
    public BookState() {
        this.state = State.READY;
        this.cardStart = 0;
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
}
