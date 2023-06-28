package fso.decor;

import java.awt.Dimension;
import java.awt.Container;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONException;

public class Utils {
    public static void centerContainer(Container container) {
        Dimension size = container.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - size.width) / 2;
        int y = (screenSize.height - size.height) / 2;
        container.setLocation(x, y);
    }

    public static boolean JSONArrayContains(JSONArray jsonArray, String element) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (jsonArray.get(i).equals(element)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public static String readAndEscape(String filePath) {
        Path path = Paths.get(filePath);
        String fileContent;
        try {
            fileContent = String.join("\n", Files.readAllLines(path));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        String ret = fileContent;
        return ret;
    }
}

