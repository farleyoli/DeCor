package fso.decor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class BookStateTest {
    private BookState state;

    @BeforeEach
    void setUp() {
        state = new BookState();
    }

    // --- Initial state ---

    @Test
    void initialStateIsReady() {
        assertEquals(State.READY, state.getState());
    }

    @Test
    void initiallyNoImageQuestion() {
        assertFalse(state.hasImageQuestion());
        assertNull(state.getImageQuestionFile());
    }

    // --- Card selection flow ---

    @Test
    void setCardStartTransitionsToWaitCardEnd() {
        state.setCardStart(0.5, 3);
        assertEquals(State.WAIT_CARD_END, state.getState());
        assertEquals(3, state.getPage());
    }

    @Test
    void setCardEndReturnsStartAndTransitionsToReady() {
        state.setCardStart(0.42, 7);
        double start = state.setCardEnd();
        assertEquals(0.42, start, 0.001);
        assertEquals(State.READY, state.getState());
    }

    @Test
    void setCardStartOnlyValidInReady() {
        state.setCardStart(0.5, 1);
        // Now in WAIT_CARD_END — calling setCardStart should fail the assertion
        assertThrows(AssertionError.class, () -> state.setCardStart(0.3, 2));
    }

    @Test
    void setCardEndOnlyValidInWaitCardEnd() {
        // In READY state — calling setCardEnd should fail
        assertThrows(AssertionError.class, () -> state.setCardEnd());
    }

    // --- Image selection flow ---

    @Test
    void startImageSelectionTransitionsToWaitImageEnd() {
        state.startImageSelection(100, 200, 5);
        assertEquals(State.WAIT_IMAGE_END, state.getState());
        assertEquals(100, state.getImageStartX());
        assertEquals(200, state.getImageStartY());
        assertEquals(5, state.getImageStartPage());
    }

    @Test
    void finishImageSelectionTransitionsToReady() {
        state.startImageSelection(100, 200, 5);
        state.finishImageSelection();
        assertEquals(State.READY, state.getState());
    }

    @Test
    void finishImageSelectionOnlyValidInWaitImageEnd() {
        assertThrows(AssertionError.class, () -> state.finishImageSelection());
    }

    @Test
    void cancelImageSelectionFromWaitImageEnd() {
        state.startImageSelection(100, 200, 5);
        state.cancelImageSelection();
        assertEquals(State.READY, state.getState());
    }

    @Test
    void cancelImageSelectionFromReadyIsNoOp() {
        state.cancelImageSelection();
        assertEquals(State.READY, state.getState());
    }

    // --- Image question file tracking ---

    @Test
    void setImageQuestionFileSetsFlag() {
        File f = new File("/tmp/test_q.jpg");
        state.setImageQuestionFile(f);
        assertTrue(state.hasImageQuestion());
        assertEquals(f, state.getImageQuestionFile());
    }

    @Test
    void clearImageQuestionResetsFlag() {
        state.setImageQuestionFile(new File("/tmp/test_q.jpg"));
        state.clearImageQuestion();
        assertFalse(state.hasImageQuestion());
        assertNull(state.getImageQuestionFile());
    }

    @Test
    void imageQuestionPersistsAcrossCardSelectionCycle() {
        state.setImageQuestionFile(new File("/tmp/test_q.jpg"));

        // Go through card selection flow
        state.setCardStart(0.5, 1);
        assertEquals(State.WAIT_CARD_END, state.getState());
        assertTrue(state.hasImageQuestion());

        state.setCardEnd();
        assertEquals(State.READY, state.getState());
        assertTrue(state.hasImageQuestion()); // Still there
    }

    // --- Interaction between image and card selection ---

    @Test
    void cannotStartCardSelectionDuringImageSelection() {
        state.startImageSelection(100, 200, 5);
        // State is WAIT_IMAGE_END, setCardStart requires READY
        assertThrows(AssertionError.class, () -> state.setCardStart(0.5, 1));
    }

    @Test
    void imageSelectionCoordsPreservedAfterFinish() {
        state.startImageSelection(150, 300, 7);
        state.finishImageSelection();
        // Coords should still be readable (used for cropping after finishImageSelection)
        assertEquals(150, state.getImageStartX());
        assertEquals(300, state.getImageStartY());
        assertEquals(7, state.getImageStartPage());
    }
}
