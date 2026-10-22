package fso.decor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

public class GlobalConfig {
    private static final HashMap<String, GlobalConfig> map = new HashMap<>();
    private final String pdfHash;
    private String pdfName = "";
    private String deckName = "";
    private static boolean isTest = false;
    private static float dialogOpacity = 1.0f;

    public static GlobalConfig getInstance(String hash) {
        map.putIfAbsent(hash, new GlobalConfig(hash));
        return map.get(hash);
    }

    public static File getImageFolder() {
        File ret = new File(getBaseFolder(), isTest ? "images-test" : "images");
        if (!ret.exists())
            ret.mkdir();
        return ret;
    }

    public static File getOsBaseFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            return getWindowsBaseFolder();
        if (os.contains("mac"))
            return getMacBaseFolder();
        if (os.contains("linux"))
            return getLinuxBaseFolder();
        // TODO: change here to folder picker
        return getLinuxBaseFolder(); 
    }

    public static File getBaseFolder() {
        File ret = new File(getOsBaseFolder(), "DeCor");
        if (!ret.exists())
            ret.mkdir();
        return ret;
    }

    public static File getPdfFolder() {
        File ret;
        if (isTest)
            ret = new File(getBaseFolder(), "pdf-files-test");
        else
            ret = new File(getBaseFolder(), "pdf-files");
        if (!ret.exists())
            ret.mkdir();
        return ret;
    }

    public static int getPageStep() {
        return 300;
    }

    public static int getDpi() {
        return 200;
    }

    public static String getModelName() {
        String ret = isTest ? "testDeCor" : "DeCor";
        return ret;
    }

    public static File getLinuxBaseFolder() {
        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome == null || xdgDataHome.isEmpty())
            xdgDataHome = System.getProperty("user.home") + "/.local/share";

        File ret = new File(xdgDataHome);

        if (!ret.exists())
            ret.mkdirs();

        return ret;
    }

    public static File getWindowsBaseFolder() {
        String localAppData = System.getenv("LOCALAPPDATA");

        File ret = new File(localAppData);

        if (!ret.exists()) {
            System.out.println("Seems like there was an error trying to find the data folder");
            System.exit(1);
        }

        return ret;
    }

    public static File getMacBaseFolder() {
        String localAppData = System.getProperty("user.home") + "/Library/Application Support/";

        File ret = new File(localAppData);

        if (!ret.exists())
            ret.mkdirs();

        return ret;
    }

    public static int getImageFileBufferSize() {
        return 15;
    }

    private GlobalConfig(String pdfHash) {
        this.pdfHash = pdfHash;
        loadDeckName();
    }

    static void setTestMode(boolean value) {
        isTest = value;
    }

    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
    }

    /**
     * Anki deck used for this PDF. Defaults to the PDF name when the user has
     * not chosen a different deck.
     */
    public String getDeckName() {
        return deckName.isBlank() ? pdfName : deckName;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName == null ? "" : deckName.trim();
        File deckNameFile = getDeckNameFile();
        try {
            if (this.deckName.isBlank()) {
                Files.deleteIfExists(deckNameFile.toPath());
            } else {
                Files.writeString(deckNameFile.toPath(), this.deckName, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not save the Anki deck name", e);
        }
    }

    private File getDeckNameFile() {
        return new File(getImageFolder(), pdfHash + ".deckname");
    }

    private void loadDeckName() {
        File deckNameFile = getDeckNameFile();
        if (!deckNameFile.exists())
            return;
        try {
            deckName = Files.readString(deckNameFile.toPath(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            System.err.println("Could not read the Anki deck name from " + deckNameFile + ": " + e.getMessage());
        }
    }

    public static float getDialogOpacity() {
        return dialogOpacity;
    }

    public static void setDialogOpacity(float opacity) {
        dialogOpacity = Math.max(0.1f, Math.min(1.0f, opacity));
    }
}
