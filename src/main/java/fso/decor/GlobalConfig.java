package fso.decor;

import java.util.HashMap;

public class GlobalConfig {
    private static final HashMap<String, GlobalConfig> map = new HashMap<>();
    private String pdfName = "";
    private static final String LINUX_BASE_DIRECTORY = System.getProperty("user.home") + "/DeCor/";

    public static GlobalConfig getInstance(String hash) {
        map.putIfAbsent(hash, new GlobalConfig());
        return map.get(hash);
    }

    public static String getImageFolder() {
        return LINUX_BASE_DIRECTORY + "images/";
    }

    public static String getBaseFolder() {
        return LINUX_BASE_DIRECTORY;
    }

    public static String getPdfFolder() {
        return LINUX_BASE_DIRECTORY + "pdf-files/"; //
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
