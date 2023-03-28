package fso.decor;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class ProgressBarContainer extends JFrame {
    public ProgressBarContainer(JProgressBar progressBar, String message) {
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JLabel messageLabel = new JLabel(message);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setSize(100, 100);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 17));
        getContentPane().add(messageLabel);
        getContentPane().add(progressBar);
        setResizable(false);
        setSize(320, 90);
    }
}
