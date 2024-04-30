package fso.decor;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class QuestionTextField extends JTextArea {
    public QuestionTextField(int width, int height) {
        super();
        setFont(new Font("SansSerif", Font.PLAIN, 17));
        setLineWrap(true);
        setWrapStyleWord(true);

        KeyAdapter keyAdapter = new KeyAdapter() { // ctrl+enter = OK
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
                    JOptionPane pane = getOptionPane((Component) e.getSource());
                    if (pane != null) {
                        pane.setValue(JOptionPane.OK_OPTION);
                    }
                }
            }
        };

        addKeyListener(keyAdapter);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!e.isShiftDown()) {  // If SHIFT is not held, insert line break
                        insert("\n", getCaretPosition());
                        e.consume();  // Prevent the dialog from closing
                    }
                }
            }
        });
    }

    private static JOptionPane getOptionPane(Component c) {
        while (c != null) {
            if (c instanceof JOptionPane) {
                return (JOptionPane) c;
            }
            c = c.getParent();
        }
        return null;
    }
}

