package fso.decor;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class GlobalConfigTest {
    private String hash;
    private File deckNameFile;

    @BeforeEach
    void setUp() {
        GlobalConfig.setTestMode(true);
        hash = "testhash_config_" + System.nanoTime();
        deckNameFile = new File(GlobalConfig.getImageFolder(), hash + ".deckname");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(deckNameFile.toPath());
        GlobalConfig.setTestMode(false);
    }

    @Test
    void deckNameDefaultsToPdfName() {
        GlobalConfig config = GlobalConfig.getInstance(hash);
        config.setPdfName("my-pdf");

        assertEquals("my-pdf", config.getDeckName());
    }

    @Test
    void deckNameIsPersisted() throws IOException {
        GlobalConfig config = GlobalConfig.getInstance(hash);
        config.setPdfName("my-pdf");

        config.setDeckName("Languages::Spanish");

        assertEquals("Languages::Spanish", config.getDeckName());
        assertTrue(deckNameFile.exists());
        assertEquals("Languages::Spanish",
                Files.readString(deckNameFile.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    void persistedDeckNameIsLoaded() throws IOException {
        Files.writeString(deckNameFile.toPath(), "Saved Deck", StandardCharsets.UTF_8);

        GlobalConfig config = GlobalConfig.getInstance(hash);
        config.setPdfName("my-pdf");

        assertEquals("Saved Deck", config.getDeckName());
    }

    @Test
    void ankiRequestUsesConfiguredDeckName() {
        GlobalConfig config = GlobalConfig.getInstance(hash);
        config.setPdfName("my-pdf");
        config.setDeckName("Languages::Spanish");

        Card card = new Card("question", "answer", 1, 0.0, 1, 1.0, 1, hash);
        JSONObject note = new JSONObject(card.getAnkiRequest("DeCor"))
                .getJSONObject("params")
                .getJSONObject("note");

        assertEquals("Languages::Spanish", note.getString("deckName"));
    }

    @Test
    void blankDeckNameFallsBackToPdfName() {
        GlobalConfig config = GlobalConfig.getInstance(hash);
        config.setPdfName("my-pdf");

        config.setDeckName("  ");

        assertEquals("my-pdf", config.getDeckName());
        assertFalse(deckNameFile.exists());
    }
}
