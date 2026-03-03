package io.github.mohammeddaniyal.chess.client.logic;

import javax.swing.*;
import java.awt.*;

public class NotificationDialog extends JDialog {
    private boolean confirmed = false;

    // blocking alert for game events
    public static void showMessage(Frame parent, String title, String message) {
        new NotificationDialog(parent, title, message, false);
    }

    // blocking yes/no prompt
    public static boolean showConfirm(Frame parent, String title, String message) {
        NotificationDialog dialog = new NotificationDialog(parent, title, message, true);
        return dialog.confirmed;
    }

    private NotificationDialog(Frame parent, String titleText, String messageText, boolean isConfirm) {
        super(parent, titleText, true); // true = modal, blocks background interaction
        setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ChessTheme.SIDEBAR_BG);
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(85, 85, 85), 2));

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setForeground(new Color(220, 220, 220));
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        mainPanel.add(title, BorderLayout.NORTH);

        JLabel message = new JLabel(messageText, SwingConstants.CENTER);
        message.setForeground(Color.WHITE);
        message.setFont(new Font("SansSerif", Font.PLAIN, 14));
        message.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        mainPanel.add(message, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(ChessTheme.SIDEBAR_BG);

        if (isConfirm) {
            JButton yesBtn = createButton("Yes", new Color(200, 70, 70));
            yesBtn.addActionListener(e -> {
                confirmed = true;
                dispose();
            });

            JButton noBtn = createButton("No", new Color(85, 88, 90));
            noBtn.addActionListener(e -> {
                confirmed = false;
                dispose();
            });

            buttonPanel.add(yesBtn);
            buttonPanel.add(noBtn);
        } else {
            JButton okBtn = createButton("OK", new Color(60, 150, 80));
            okBtn.addActionListener(e -> dispose());
            buttonPanel.add(okBtn);
        }

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setMinimumSize(new Dimension(300, 150));
        setLocationRelativeTo(parent);
        setVisible(true); // thread waits here until disposed
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}