package fso.decor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {
    private static final String HASH = "abc123";

    private Card card(String front, String back, int begPage, double begPct, int endPage, double endPct, int id) {
        return new Card(front, back, begPage, begPct, endPage, endPct, id, HASH);
    }

    // --- Sanitization ---

    @Test
    void sanitizeStripsWhitespace() {
        Card c = card("  hello  ", "  world  ", 1, 0.0, 1, 1.0, 1);
        assertEquals("hello", c.getFront());
        assertEquals("world", c.getBack());
    }

    @Test
    void sanitizeReplacesNewlinesWithBr() {
        Card c = card("line1\nline2\nline3", "a\nb", 1, 0.0, 1, 1.0, 1);
        assertEquals("line1<br>line2<br>line3", c.getFront());
        assertEquals("a<br>b", c.getBack());
    }

    @Test
    void sanitizeRemovesDelimiter() {
        Card c = card("hello‽world", "foo‽bar", 1, 0.0, 1, 1.0, 1);
        assertEquals("helloworld", c.getFront());
        assertEquals("foobar", c.getBack());
    }

    @Test
    void sanitizeEmptyString() {
        Card c = card("", "", 1, 0.0, 1, 1.0, 1);
        assertEquals("", c.getFront());
        assertEquals("", c.getBack());
    }

    @Test
    void sanitizePreservesImgTag() {
        String imgTag = "<img src=\"hash_q_123.jpg\">";
        Card c = card(imgTag, "answer", 1, 0.0, 1, 1.0, 1);
        assertEquals(imgTag, c.getFront());
    }

    // --- Intersection ---

    @Test
    void intersectsSinglePageInside() {
        Card c = card("q", "a", 5, 0.2, 5, 0.8, 1);
        assertTrue(c.intersects(5, 0.5));
    }

    @Test
    void intersectsSinglePageAtBoundaries() {
        Card c = card("q", "a", 5, 0.2, 5, 0.8, 1);
        assertTrue(c.intersects(5, 0.2));
        assertTrue(c.intersects(5, 0.8));
    }

    @Test
    void intersectsSinglePageOutside() {
        Card c = card("q", "a", 5, 0.2, 5, 0.8, 1);
        assertFalse(c.intersects(5, 0.1));
        assertFalse(c.intersects(5, 0.9));
    }

    @Test
    void intersectsWrongPage() {
        Card c = card("q", "a", 5, 0.2, 5, 0.8, 1);
        assertFalse(c.intersects(4, 0.5));
        assertFalse(c.intersects(6, 0.5));
    }

    @Test
    void intersectsMultiPageStartPage() {
        Card c = card("q", "a", 3, 0.5, 5, 0.5, 1);
        assertTrue(c.intersects(3, 0.5));
        assertTrue(c.intersects(3, 0.9));
        assertFalse(c.intersects(3, 0.4));
    }

    @Test
    void intersectsMultiPageEndPage() {
        Card c = card("q", "a", 3, 0.5, 5, 0.5, 1);
        assertTrue(c.intersects(5, 0.5));
        assertTrue(c.intersects(5, 0.1));
        assertFalse(c.intersects(5, 0.6));
    }

    @Test
    void intersectsMultiPageMiddlePage() {
        Card c = card("q", "a", 3, 0.5, 5, 0.5, 1);
        assertTrue(c.intersects(4, 0.0));
        assertTrue(c.intersects(4, 1.0));
        assertTrue(c.intersects(4, 0.5));
    }

    @Test
    void intersectsMultiPageOutsidePages() {
        Card c = card("q", "a", 3, 0.5, 5, 0.5, 1);
        assertFalse(c.intersects(2, 0.5));
        assertFalse(c.intersects(6, 0.5));
    }

    // --- Serialization ---

    @Test
    void serializationFormat() {
        Card c = card("question", "answer", 1, 0.25, 3, 0.75, 42);
        String s = c.getSerialiseString("‽");
        assertEquals("question‽answer‽1‽0.25‽3‽0.75‽42", s);
    }

    @Test
    void serializationWithSanitizedContent() {
        Card c = card("q\ntext", "a‽b", 1, 0.0, 1, 1.0, 1);
        String s = c.getSerialiseString("‽");
        // newline -> <br>, delimiter removed
        assertEquals("q<br>text‽ab‽1‽0.0‽1‽1.0‽1", s);
    }

    @Test
    void serializationWithImgTag() {
        String img = "<img src=\"hash_q_999.jpg\">";
        Card c = card(img, "answer", 2, 0.1, 2, 0.9, 7);
        String s = c.getSerialiseString("‽");
        assertTrue(s.startsWith(img + "‽"));
    }

    // --- Other ---

    @Test
    void newCardIsNew() {
        Card c = card("q", "a", 1, 0.0, 1, 1.0, 1);
        assertTrue(c.isNew());
    }

    @Test
    void cardWithIsNewFalse() {
        Card c = new Card("q", "a", 1, 0.0, 1, 1.0, 1, HASH, false);
        assertFalse(c.isNew());
    }

    @Test
    void setNewChangesFlag() {
        Card c = card("q", "a", 1, 0.0, 1, 1.0, 1);
        assertTrue(c.isNew());
        c.setNew(false);
        assertFalse(c.isNew());
    }

    @Test
    void gettersReturnCorrectValues() {
        Card c = card("front", "back", 2, 0.3, 4, 0.7, 10);
        assertEquals("front", c.getFront());
        assertEquals("back", c.getBack());
        assertEquals(2, c.getBeginningPage());
        assertEquals(0.3, c.getBeginningPercentage(), 0.001);
        assertEquals(4, c.getEndPage());
        assertEquals(0.7, c.getEndPercentage(), 0.001);
        assertEquals(10, c.getId());
    }
}
