package io.github.mohammeddaniyal.chess.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton connectButton;
    private JLabel statusLabel;

    private final Color BACKGROUND_COLOR = new Color(43, 43, 43);
    private final Color PANEL_BG = new Color(60, 63, 65);
    private final Color TEXT_COLOR = new Color(220, 220, 220);
    private final Color ACCENT_COLOR = new Color(72, 118, 168); 

    public LoginPanel(ActionListener onConnectAction) {
        setBackground(BACKGROUND_COLOR);
        setLayout(new GridBagLayout()); // centers the login container

        JPanel loginBox = new JPanel();
        loginBox.setLayout(new BoxLayout(loginBox, BoxLayout.Y_AXIS));
        loginBox.setBackground(PANEL_BG);
        loginBox.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40)); 

        JLabel titleLabel = new JLabel("TM Chess Engine");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = new JTextField(15);
        styleTextField(usernameField);

        passwordField = new JPasswordField(15);
        styleTextField(passwordField);
        
        JCheckBox showPassword = new JCheckBox("Show Password");
        showPassword.setBackground(BACKGROUND_COLOR); 
        showPassword.setForeground(TEXT_COLOR); 
        showPassword.setFont(new Font("SansSerif", Font.PLAIN, 12));
        showPassword.setFocusPainted(false);
        
        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });

        JPanel checkWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        checkWrapper.setBackground(PANEL_BG);
        checkWrapper.setMaximumSize(new Dimension(350, 30));
        checkWrapper.add(showPassword);

        connectButton = new JButton("Connect to Server");
        connectButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        connectButton.setBackground(ACCENT_COLOR);
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        connectButton.setMaximumSize(new Dimension(350, 40));
        connectButton.setPreferredSize(new Dimension(350, 40));
        connectButton.addActionListener(onConnectAction);
        
        // bind enter key to connect button
        SwingUtilities.invokeLater(() -> {
            getRootPane().setDefaultButton(connectButton);
        });

        statusLabel = new JLabel(" "); 
        statusLabel.setForeground(new Color(255, 100, 100)); 
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBox.add(titleLabel);
        loginBox.add(Box.createVerticalStrut(30)); 
        loginBox.add(createLabeledField("Username:", usernameField));
        loginBox.add(Box.createVerticalStrut(15));
        loginBox.add(createLabeledField("Password:", passwordField));
        loginBox.add(checkWrapper);
        loginBox.add(Box.createVerticalStrut(15));
        loginBox.add(connectButton);
        loginBox.add(Box.createVerticalStrut(15));
        loginBox.add(statusLabel);

        add(loginBox);
    }

    private JPanel createLabeledField(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(PANEL_BG);

        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(350, 65));
        panel.setPreferredSize(new Dimension(350, 65));
        return panel;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(new Color(69, 73, 74));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(85, 85, 85), 1),
                BorderFactory.createEmptyBorder(5, 5, 8, 5)
        ));
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public void showError(String message) {
        statusLabel.setText(message);
    }

    public void resetFields() {
        usernameField.setText("");
        passwordField.setText("");
        statusLabel.setText(" "); 
    }
}