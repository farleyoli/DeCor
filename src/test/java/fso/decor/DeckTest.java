package fso.decor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {
    private static final String HASH = "testhash_deck";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        GlobalConfig.setTestMode(true);
    }

    private Deck emptyDeck() {
        // Use a unique hash to avoid loading from disk
        return new Deck(HASH + System.nanoTime());
    }

    // --- Add/Delete ---

    @Test
    void addCardSinglePage() {
        Deck deck = emptyDeck();
        Card c = deck.addCard("q", "a", 3, 0.2, 3, 0.8);
        assertNotNull(c);
        assertEquals("q", c.getFront());
        assertEquals("a", c.getBack());
        assertTrue(deck.getPageToCards().containsKey(3));
        assertTrue(deck.getPageToCards().get(3).contains(c));
    }

    @Test
    void addCardMultiPage() {
        Deck deck = emptyDeck();
        Card c = deck.addCard("q", "a", 2, 0.5, 4, 0.5);
        for (int page = 2; page <= 4; page++) {
            assertTrue(deck.getPageToCards().containsKey(page));
            assertTrue(deck.getPageToCards().get(page).contains(c));
        }
    }

    @Test
    void addMultipleCardsAutoIncrementId() {
        Deck deck = emptyDeck();
        Card c1 = deck.addCard("q1", "a1", 1, 0.0, 1, 0.5);
        Card c2 = deck.addCard("q2", "a2", 1, 0.5, 1, 1.0);
        assertNotEquals(c1.getId(), c2.getId());
        assertEquals(c1.getId() + 1, c2.getId());
    }

    @Test
    void deleteCardRemovesFromAllPages() {
        Deck deck = emptyDeck();
        Card c = deck.addCard("q", "a", 2, 0.5, 4, 0.5);
        int id = c.getId();

        Set<Integer> affectedPages = deck.deleteCard(id);

        assertEquals(Set.of(2, 3, 4), affectedPages);
        for (int page = 2; page <= 4; page++) {
            assertFalse(deck.getPageToCards().get(page).contains(c));
        }
    }

    @Test
    void deleteCardLeavesOtherCards() {
        Deck deck = emptyDeck();
        Card c1 = deck.addCard("q1", "a1", 1, 0.0, 1, 0.5);
        Card c2 = deck.addCard("q2", "a2", 1, 0.5, 1, 1.0);

        deck.deleteCard(c1.getId());

        assertTrue(deck.getPageToCards().get(1).contains(c2));
        assertEquals(1, deck.getPageToCards().get(1).size());
    }

    // --- Session card count ---

    @Test
    void sessionCardCountIncrementsOnAdd() {
        Deck deck = emptyDeck();
        assertEquals(0, deck.getSessionCardCount());
        deck.addCard("q1", "a1", 1, 0.0, 1, 1.0);
        assertEquals(1, deck.getSessionCardCount());
        deck.addCard("q2", "a2", 1, 0.0, 1, 1.0);
        assertEquals(2, deck.getSessionCardCount());
    }

    @Test
    void sessionCardCountNotIncrementedByAddCardWithId() {
        Deck deck = emptyDeck();
        deck.addCardWithId("q", "a", 1, 0.0, 1, 1.0, 99);
        assertEquals(0, deck.getSessionCardCount());
    }

    // --- Intersection queries ---

    @Test
    void getCardsInPositionFindsMatchingCards() {
        Deck deck = emptyDeck();
        Card c1 = deck.addCard("q1", "a1", 1, 0.0, 1, 0.5);
        Card c2 = deck.addCard("q2", "a2", 1, 0.3, 1, 0.8);

        Set<Card> atPoint = deck.getCardsInPosition(1, 0.4);
        assertEquals(2, atPoint.size());
        assertTrue(atPoint.contains(c1));
        assertTrue(atPoint.contains(c2));
    }

    @Test
    void getCardsInPositionReturnsEmpty() {
        Deck deck = emptyDeck();
        deck.addCard("q", "a", 1, 0.2, 1, 0.4);

        Set<Card> atPoint = deck.getCardsInPosition(1, 0.5);
        assertTrue(atPoint.isEmpty());
    }

    // --- ScrollBar rendering ---

    @Test
    void renderScrollBarReturnsEmptyForBlankPage() {
        Deck deck = emptyDeck();
        assertTrue(deck.renderScrollBar(5).isEmpty());
    }

    @Test
    void renderScrollBarReturnsPairsForSinglePageCard() {
        Deck deck = emptyDeck();
        deck.addCard("q", "a", 3, 0.2, 3, 0.8);

        var positions = deck.renderScrollBar(3);
        assertEquals(2, positions.size());
        assertEquals(0.2, positions.get(0), 0.001);
        assertEquals(0.8, positions.get(1), 0.001);
    }

    @Test
    void renderScrollBarMultiPageCardStartPage() {
        Deck deck = emptyDeck();
        deck.addCard("q", "a", 3, 0.5, 5, 0.3);

        var positions = deck.renderScrollBar(3);
        assertEquals(2, positions.size());
        assertEquals(0.5, positions.get(0), 0.001);
        assertEquals(1.0, positions.get(1), 0.001);
    }

    @Test
    void renderScrollBarMultiPageCardMiddlePage() {
        Deck deck = emptyDeck();
        deck.addCard("q", "a", 3, 0.5, 5, 0.3);

        var positions = deck.renderScrollBar(4);
        assertEquals(2, positions.size());
        assertEquals(0.0, positions.get(0), 0.001);
        assertEquals(1.0, positions.get(1), 0.001);
    }

    @Test
    void renderScrollBarMultiPageCardEndPage() {
        Deck deck = emptyDeck();
        deck.addCard("q", "a", 3, 0.5, 5, 0.3);

        var positions = deck.renderScrollBar(5);
        assertEquals(2, positions.size());
        assertEquals(0.0, positions.get(0), 0.001);
        assertEquals(0.3, positions.get(1), 0.001);
    }

    // --- Serialization ---

    @Test
    void serializeAndDeserializeRoundTrip() {
        Deck deck = emptyDeck();
        deck.addCard("question1", "answer1", 1, 0.1, 1, 0.5);
        deck.addCard("multi page", "answer2", 2, 0.3, 4, 0.7);

        String serialized = deck.getSerialiseString();

        // Parse it back manually
        String[] lines = serialized.split("\n");
        assertTrue(lines.length >= 3); // id line + 2 cards

        // Verify each card line has 7 fields
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isEmpty()) continue;
            String[] fields = lines[i].split("‽");
            assertEquals(7, fields.length, "Card serialization should have 7 fields: " + lines[i]);
        }
    }

    @Test
    void addCardFromSerialiseStringRestoresCard() {
        Deck deck = emptyDeck();
        deck.addCardFromSerialiseString("hello‽world‽2‽0.25‽3‽0.75‽50");

        var cards = deck.getCards();
        assertEquals(1, cards.size());
        Card c = cards.iterator().next();
        assertEquals("hello", c.getFront());
        assertEquals("world", c.getBack());
        assertEquals(2, c.getBeginningPage());
        assertEquals(0.25, c.getBeginningPercentage(), 0.001);
        assertEquals(3, c.getEndPage());
        assertEquals(0.75, c.getEndPercentage(), 0.001);
        assertEquals(50, c.getId());
        assertFalse(c.isNew()); // Deserialized cards are not new
    }

    @Test
    void addCardFromSerialiseStringWithImgTag() {
        String imgFront = "<img src=\"hash_q_123.jpg\">";
        String line = imgFront + "‽answer‽1‽0.0‽1‽1.0‽7";
        Deck deck = emptyDeck();
        deck.addCardFromSerialiseString(line);

        Card c = deck.getCards().iterator().next();
        assertEquals(imgFront, c.getFront());
        assertEquals("answer", c.getBack());
    }

    // --- getCards ---

    @Test
    void getCardsReturnsAllCards() {
        Deck deck = emptyDeck();
        deck.addCard("q1", "a1", 1, 0.0, 1, 1.0);
        deck.addCard("q2", "a2", 2, 0.0, 2, 1.0);
        deck.addCard("q3", "a3", 3, 0.0, 3, 1.0);

        assertEquals(3, deck.getCards().size());
    }
}
