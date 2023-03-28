package fso.decor;

import java.awt.Dimension;
import java.awt.Container;
import java.awt.Toolkit;

public class Utils {
    public static void centerContainer(Container container) {
        Dimension size = container.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - size.width) / 2;
        int y = (screenSize.height - size.height) / 2;
        container.setLocation(x, y);
    }
}
