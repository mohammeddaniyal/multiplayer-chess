package io.github.mohammeddaniyal.chess.client.logic;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ChessTheme {
    // board colors
    public static final Color DARK_TILE = new Color(118, 150, 86);
    public static final Color LIGHT_TILE = new Color(238, 238, 210);
    public static final Color SIDEBAR_BG = new Color(43, 43, 43);
    
    public static final Color HIGHLIGHT_SELECTED = new Color(246, 246, 105);

    // 5px total size so pieces don't shift during interaction when borders swap
    public static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    private static Border createSpacedBorder(Color color) {
        // 2px outer gap + 3px inner line = 5px total (matches EMPTY_BORDER)
        return BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 2, 2, 2), 
            BorderFactory.createLineBorder(color, 3)
        );
    }

    public static final Border BORDER_MOVE = createSpacedBorder(new Color(25, 75, 40)); 
    public static final Border BORDER_CAPTURE = createSpacedBorder(new Color(205, 92, 92)); 
    public static final Border BORDER_CASTLING = createSpacedBorder(new Color(218, 165, 32)); 

    public static ImageIcon loadIcon(String filename) {
        try {
            return new ImageIcon(ChessTheme.class.getResource("/icons/" + filename));
        } catch (Exception e) {
            System.err.println("Could not load icon: " + filename);
            return null;
        }
    }
}