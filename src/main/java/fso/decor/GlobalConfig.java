package fso.decor;

import java.io.File;
import java.util.HashMap;

public class GlobalConfig {
    private static final HashMap<String, GlobalConfig> map = new HashMap<>();
    private String pdfName = "";
    private static boolean isTest = true;

    public static GlobalConfig getInstance(String hash) {
        map.putIfAbsent(hash, new GlobalConfig());
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

    public static String getDeckName() {
        return "DeCor";
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

    public GlobalConfig() {
    }

    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
    }
}
