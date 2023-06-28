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

    public static String quote(String string) {
        // https://stackoverflow.com/questions/3020094/how-should-i-escape-strings-in-json
        if (string == null || string.length() == 0) {
            return "";
        }

        char         c = 0;
        int          i;
        int          len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String       t;

        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    //                if (b == '<') {
                    sb.append('\\');
                    //                }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
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

