package io.github.mohammeddaniyal.chess.client.logic;

import javax.swing.*;
import java.awt.*;

public class PawnPromotionDialog extends JDialog {
    private String promoteToName;
    private ImageIcon promoteToIcon;
    private byte selectedPiece;

    public PawnPromotionDialog(Frame parentWindow, String color) {
        super(parentWindow, "Promote Pawn", true); // modal

        setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ChessTheme.SIDEBAR_BG);
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(85, 85, 85), 2));

        JLabel title = new JLabel("Choose Promotion", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(title, BorderLayout.NORTH);

        // 1x4 layout for promotion options
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        buttonPanel.setBackground(ChessTheme.SIDEBAR_BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 20, 15));

        String prefix = color.equals("white") ? "white" : "black";
        byte queenByte = (byte) (color.equals("white") ? 5 : -5);
        byte bishopByte = (byte) (color.equals("white") ? 3 : -3);
        byte rookByte = (byte) (color.equals("white") ? 4 : -4);
        byte knightByte = (byte) (color.equals("white") ? 2 : -2);

        buttonPanel.add(createOptionButton(prefix + "_queen.png", prefix + "Queen", queenByte));
        buttonPanel.add(createOptionButton(prefix + "_knight.png", prefix + "Knight", knightByte));
        buttonPanel.add(createOptionButton(prefix + "_rook.png", prefix + "Rook", rookByte));
        buttonPanel.add(createOptionButton(prefix + "_bishop.png", prefix + "Bishop", bishopByte));

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // default to queen if dialog is closed unexpectedly
        promoteToName = prefix + "Queen";
        promoteToIcon = ChessTheme.loadIcon(prefix + "_queen.png");
        selectedPiece = queenByte;

        add(mainPanel);
        pack();
        setLocationRelativeTo(parentWindow);
        setVisible(true); // blocks thread until selection is made
    }

    private JButton createOptionButton(String iconName, String pieceName, byte pieceByte) {
        ImageIcon icon = ChessTheme.loadIcon(iconName);
        JButton btn = new JButton(icon);
        btn.setBackground(new Color(60, 63, 65));
        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(70, 70));

        btn.addActionListener(e -> {
            this.promoteToName = pieceName;
            this.promoteToIcon = icon;
            this.selectedPiece = pieceByte;
            dispose();
        });
        return btn;
    }

    public byte getSelectedPiece() {
        return selectedPiece;
    }

    public String getPromoteToName() {
        return promoteToName;
    }

    public ImageIcon getPromoteToIcon() {
        return promoteToIcon;
    }
}