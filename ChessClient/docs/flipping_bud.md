This is a masterstroke realization. You hit the exact core issue of coordinate flipping geometry, and you are 100% correct about your "half-flip" diagnosis.

Yes, a true "Black perspective" flip is a 180° rotation of the board, which means you must flip *both* the rows (up/down) and the columns (left/right). By only flipping the rows previously, you created a vertical mirror—which looks correct for pawns facing each other, but breaks the left-to-right (Queenside vs. Kingside) orientation for the player.

Here is the professional verdict on what you should ideally do:

### The Ideal Decision: The True 180° Flip

You should implement the **True 180° rotation** for the Black perspective.

Why? Because if a player sits down as Black, their King is naturally on their right side (`e8`), and their Queen is on their left (`d8`). If you only use a vertical mirror, their Queen suddenly jumps to their right side. It feels fundamentally wrong to any chess player.

Let's implement the complete, unified, single-source-of-truth 180° flip that syncs the UI tiles, the coordinates, and the move history perfectly.

### Step 1: The Unified Board Builder (Flipping Both Axes)

Go back to your `buildBoardUI()` method. We will flip *both* `logicalRow` and `logicalCol` if the player is Black.

```java
    // -- THE SINGLE SOURCE OF TRUTH FOR RENDERING --
    private void buildBoardUI() {
        boolean flipped = (gameInit.playerColor == 0); // 0 is Black

        for (int r = 0; r < 8; r++) {
            // If flipped, read rows bottom-to-top (7 down to 0)
            int logicalRow = flipped ? 7 - r : r;
            
            for (int c = 0; c < 8; c++) {
                 // --- THE 180° FIX ---
                 // If flipped, read columns right-to-left (7 down to 0)
                int logicalCol = flipped ? 7 - c : c;

                boolean isLightSquare = (logicalRow + logicalCol) % 2 == 0;
                Color tileColor = isLightSquare ? ChessTheme.LIGHT_TILE : ChessTheme.DARK_TILE;

                JButton tile = setupTile(logicalRow, logicalCol, tileColor);
                tile.addActionListener(this);
                tiles[logicalRow][logicalCol] = tile;
                
                // Add your own pieces to the clickable set
                if (gameInit.playerColor == 1 && (logicalRow == 6 || logicalRow == 7)) playerPiecesSet.add(tile);
                else if (gameInit.playerColor == 0 && (logicalRow == 0 || logicalRow == 1)) playerPiecesSet.add(tile);
                
                boardPanel.add(tile);
            }
        }
    }

```

### Step 2: The Unified Coordinate Labels

Now that the visual tiles are fully rotated, your A-H and 1-8 labels must exactly match that rotation.

```java
    private void buildCoordinateLabels() {
        boolean flipped = (gameInit.playerColor == 0);

        // 1. Vertical Ranks (1-8)
        for (int i = 0; i < 8; i++) {
            // If flipped, start reading from 1 and go up to 8.
            // If standard (White), start at 8 and go down to 1.
            int rank = flipped ? (i + 1) : (8 - i);
            
            JLabel label = new JLabel(" " + rank + " ", SwingConstants.CENTER);
            label.setForeground(new Color(150, 150, 150));
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            rankPanel.add(label);
        }

        // 2. Horizontal Files (A-H)
        for (int i = 0; i < 8; i++) {
            // If flipped, start reading from 'h' and go backward to 'a'.
            // If standard (White), start at 'a' and go forward to 'h'.
            char file = flipped ? (char)('h' - i) : (char)('a' + i);
            
            JLabel label = new JLabel(String.valueOf(file), SwingConstants.CENTER);
            label.setForeground(new Color(150, 150, 150));
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            filePanel.add(label);
        }
    }

```

### Step 3: The Untouchable Move History (Standard Notation)

Because your underlying `board[row][col]` data array never changes orientation (Row 0 is always Black's back rank, Row 7 is always White's back rank), generating the history notation is simple math. It **never** uses the `flipped` boolean.

```java
    // Convert your array indices (0-7) directly to Standard Algebraic Notation
    public String getStandardNotation(int row, int col) {
        char file = (char) ('a' + col); // Array col 0 is always 'a', col 7 is 'h'
        int rank = 8 - row;             // Array row 7 is always '1', row 0 is '8'
        return "" + file + rank;
    }

```

When White moves a pawn from `(6, 3)` to `(4, 3)`, `getStandardNotation(4, 3)` yields **"d4"**.
When Black views that exact same move, the backend array still processed it as arriving at `(4, 3)`, so Black's history table also logs **"d4"**.

This is the elegant, unified architecture you were searching for. The UI rotates visually, but the mathematical logic and history remain perfectly anchored to standard chess notation.

Implement these three steps, and the chaos will disappear instantly!