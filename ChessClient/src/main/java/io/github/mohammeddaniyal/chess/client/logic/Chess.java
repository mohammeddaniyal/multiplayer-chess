package io.github.mohammeddaniyal.chess.client.logic;

import io.github.mohammeddaniyal.nframework.client.*;
import io.github.mohammeddaniyal.chess.client.history.*;
import io.github.mohammeddaniyal.chess.client.MainApplicationFrame;
import io.github.mohammeddaniyal.chess.common.*;

import javax.swing.*;
import javax.swing.Timer; // explicitly imported to resolve clash with java.util.Timer
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Chess extends JPanel implements ActionListener {
    
    private MainApplicationFrame mainApplicationFrame;
    private NFrameworkClient client;
    private GameInit gameInit;
    private String username;
    private String title;

    private JPanel boardPanel;
    private JButton[][] tiles;
    private JLabel statusLabel;
    private MoveHistoryPanel moveHistoryPanel;
    
    private Set<JButton> playerPiecesSet;
    private boolean isPlayerTurnHighlighted = false;
    private Map<Byte, String> pieceNamesMap;
    private byte[][] possibleMoves;
    private boolean canIPlay;
    private byte firstTurnOfPlayerColor;

    private JButton sourceTile = null;
    private JButton targetTile = null;
    private boolean click = false;
    private byte startRowIndex, startColumnIndex, destinationRowIndex, destinationColumnIndex;

    private Timer getOpponentMoveTimer;
    private Timer isOpponentLeftTheGameTimer;

    public Chess(MainApplicationFrame mainFrame, NFrameworkClient client, GameInit gameInit, String username) {
        this.mainApplicationFrame = mainFrame;
        this.client = client;
        this.gameInit = gameInit;
        this.username = username;
        this.title = "Member: " + username;
        
        populateDataStructures();
        this.playerPiecesSet = new HashSet<>();
        
        setLayout(new BorderLayout());
        setBackground(ChessTheme.SIDEBAR_BG);
        
        tiles = new JButton[8][8];
        boardPanel = new JPanel(new GridLayout(8, 8));
        buildBoardUI();
        
        JPanel boardWrapper = new JPanel(new BorderLayout());
        boardWrapper.setBackground(ChessTheme.SIDEBAR_BG);

        // rank labels (1-8)
        JPanel rankPanel = new JPanel(new GridLayout(8, 1));
        rankPanel.setBackground(ChessTheme.SIDEBAR_BG);
        for (int i = 0; i < 8; i++) {
            int rank = (gameInit.playerColor == 0) ? (i + 1) : (8 - i);
            JLabel label = new JLabel(" " + rank + " ", SwingConstants.CENTER);
            label.setForeground(new Color(150, 150, 150));
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            rankPanel.add(label);
        }

        // file labels (a-h)
        JPanel filePanel = new JPanel(new GridLayout(1, 8));
        filePanel.setBackground(ChessTheme.SIDEBAR_BG);
        filePanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0)); // align with grid
        for (int i = 0; i < 8; i++) {
            char file = (gameInit.playerColor == 0) ? (char)('h' - i) : (char)('a' + i);
            JLabel label = new JLabel(String.valueOf(file), SwingConstants.CENTER);
            label.setForeground(new Color(150, 150, 150));
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            filePanel.add(label);
        }

        boardWrapper.add(rankPanel, BorderLayout.WEST);
        boardWrapper.add(filePanel, BorderLayout.SOUTH);
        boardWrapper.add(boardPanel, BorderLayout.CENTER); 

        add(boardWrapper, BorderLayout.CENTER);

        JPanel sidebarPanel = new JPanel(new BorderLayout(0, 10));
        sidebarPanel.setBackground(ChessTheme.SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(280, 0)); 
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Initializing Game...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        statusLabel.setForeground(new Color(220, 220, 220));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        sidebarPanel.add(statusLabel, BorderLayout.NORTH);

        canIPlayCheckInit();
        moveHistoryPanel = new MoveHistoryPanel(firstTurnOfPlayerColor);
        sidebarPanel.add(moveHistoryPanel, BorderLayout.CENTER);

        JButton resignButton = new JButton("Resign / Leave Game");
        resignButton.setBackground(new Color(200, 70, 70));
        resignButton.setForeground(Color.WHITE);
        resignButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        resignButton.setFocusPainted(false);
        resignButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resignButton.setPreferredSize(new Dimension(0, 45));
        resignButton.addActionListener(e -> handleResign());
        sidebarPanel.add(resignButton, BorderLayout.SOUTH);

        add(sidebarPanel, BorderLayout.EAST);
        
        addActionListeners();
    }

    private void buildBoardUI() {
        boolean flipped = (gameInit.playerColor == 0);
        for (int r = 0; r < 8; r++) {
            int logicalRow = flipped ? 7 - r : r;
            
            for (int c = 0; c < 8; c++) {
                // flip columns to rotate perspective entirely
                int logicalCol = flipped ? 7 - c : c;

                boolean isLightSquare = (logicalRow + logicalCol) % 2 == 0;
                Color tileColor = isLightSquare ? ChessTheme.LIGHT_TILE : ChessTheme.DARK_TILE;

                JButton tile = setupTile(logicalRow, logicalCol, tileColor);
                tile.addActionListener(this);
                
                tiles[logicalRow][logicalCol] = tile;
                
                // track playable pieces
                if (gameInit.playerColor == 1 && (logicalRow == 6 || logicalRow == 7)) playerPiecesSet.add(tile);
                else if (gameInit.playerColor == 0 && (logicalRow == 0 || logicalRow == 1)) playerPiecesSet.add(tile);
                
                boardPanel.add(tile);
            }
        }
    }

    private JButton setupTile(int r, int c, Color tileColor) {
        byte piece = gameInit.board[r][c];
        JButton tile = new JButton();
        tile.setOpaque(true);
        tile.setFocusPainted(false);
        tile.setContentAreaFilled(true);    
        tile.setBackground(tileColor);
        tile.setBorder(ChessTheme.EMPTY_BORDER);
        tile.setLayout(new BorderLayout());

        if (piece != 0) {
            String pieceName = getPieceName(piece);
            tile.setActionCommand(pieceName);
            
            String prefix = piece > 0 ? "white" : "black";
            String type = pieceNamesMap.get((byte) Math.abs(piece)).toLowerCase();
            tile.add(new JLabel(ChessTheme.loadIcon(prefix + "_" + type + ".png")));
        } else {
            tile.setActionCommand("");
        }
        return tile;
    }

    private void populateDataStructures() {
        this.pieceNamesMap = Map.ofEntries(
            Map.entry((byte) 1, "Pawn"), Map.entry((byte) 2, "Knight"),
            Map.entry((byte) 3, "Bishop"), Map.entry((byte) 4, "Rook"),
            Map.entry((byte) 5, "Queen"), Map.entry((byte) 6, "King")
        );
    }

    private String getPieceName(byte piece) {
        if (piece == 0) return "";
        return (piece > 0 ? "white" : "black") + pieceNamesMap.get((byte) Math.abs(piece));
    }

    private void canIPlayCheckInit() {
        try {
            this.canIPlay = (boolean) client.execute("/ChessServer/canIPlay", gameInit.gameId, gameInit.playerColor);
            firstTurnOfPlayerColor = canIPlay ? gameInit.playerColor : (byte) ((gameInit.playerColor == 1) ? 0 : 1);
            
            String myColor = gameInit.playerColor == 0 ? "Black" : "White";
            if (!canIPlay) {
                statusLabel.setText("<html><div style='text-align: center;'>Opponent's Turn<br><span style='font-size:12px; color:#A0A0A0;'>Playing as " + myColor + "</span></div></html>");
            } else {
                highlightTurn();
                statusLabel.setText("<html><div style='text-align: center; color:#90EE90;'>Your Turn!<br><span style='font-size:12px; color:#A0A0A0;'>Playing as " + myColor + "</span></div></html>");
            }
        } catch (Throwable t) {
            System.err.println("Init Network Error: " + t.getMessage());
            NotificationDialog.showMessage(mainApplicationFrame, "Connection Error", "Cannot reach the game server.");
        }
    }

    private void addActionListeners() {
        isOpponentLeftTheGameTimer = new Timer(1000, ev -> {
            try {
                byte left = (byte) client.execute("/ChessServer/isOpponentLeftTheGame", gameInit.gameId, username);
                if (left == 1) {
                    ((Timer) ev.getSource()).stop();
                    client.execute("/ChessServer/leftGame", username);
                    
                    // return to lobby before showing alert to prevent desync
                    SwingUtilities.invokeLater(() -> {
                        mainApplicationFrame.returnToLobbyFromGame();
                        NotificationDialog.showMessage(mainApplicationFrame, "Game Over", "Opponent left the game. You Won!");
                    });
                }
            } catch (Throwable t) { System.err.println("Network blip during polling. Retrying..."); }
        });
        isOpponentLeftTheGameTimer.start();

        getOpponentMoveTimer = new Timer(1000, ev -> {
            try {
                Move move = (Move) client.execute("/ChessServer/getOpponentMove", gameInit.gameId, gameInit.playerColor);
                if (move == null) return;

                ((Timer) ev.getSource()).stop();
                handleMoveHistory(move);
                
                this.sourceTile = tiles[move.fromX][move.fromY];
                this.targetTile = tiles[move.toX][move.toY];
                this.startRowIndex = move.fromX;
                this.startColumnIndex = move.fromY;
                this.destinationRowIndex = move.toX;
                this.destinationColumnIndex = move.toY;
                
                movePiece(getPieceName(move.piece));
                updateBoardState(move);

                if (move.pawnPromotionTo != 0) {
                    String pName = getPieceName(move.pawnPromotionTo);
                    String prefix = move.pawnPromotionTo > 0 ? "white" : "black";
                    String type = pieceNamesMap.get((byte) Math.abs(move.pawnPromotionTo)).toLowerCase();
                    ImageIcon pIcon = ChessTheme.loadIcon(prefix + "_" + type + ".png");
                    applyPromotion(pName, pIcon);
                }

                if (move.isLastMove == 1) {
                    isOpponentLeftTheGameTimer.stop();
                    SwingUtilities.invokeLater(() -> {
                        mainApplicationFrame.returnToLobbyFromGame();
                        NotificationDialog.showMessage(mainApplicationFrame, "Game Over", "You Lost!");
                    });
                    return;
                }

                resetSelection();
                canIPlay = true;
                highlightTurn();
                
                String myColor = gameInit.playerColor == 0 ? "Black" : "White";
                statusLabel.setText("<html><div style='text-align: center; color:#90EE90;'>Your Turn!<br><span style='font-size:12px; color:#A0A0A0;'>Playing as " + myColor + "</span></div></html>");

                byte stalemate = (byte) client.execute("/ChessServer/isStalemate", gameInit.gameId, gameInit.playerColor);
                if (stalemate == 1) {
                    isOpponentLeftTheGameTimer.stop();
                    SwingUtilities.invokeLater(() -> {
                        mainApplicationFrame.returnToLobbyFromGame();
                        NotificationDialog.showMessage(mainApplicationFrame, "Stalemate. The game is a draw", "Game Over");
                    });
                }
            } catch (Throwable t) { System.err.println("Network blip during polling. Retrying..."); }
        });
        if (!canIPlay) getOpponentMoveTimer.start();
    }

    public void actionPerformed(ActionEvent ev) {
        if (!canIPlay) return;

        JButton tile = (JButton) ev.getSource();
        
        byte r = 0, c = 0;
        boolean found = false;
        for (r = 0; r < 8; r++) {
            for (c = 0; c < 8; c++) {
                if (tiles[r][c] == tile) { found = true; break; }
            }
            if (found) break;
        }

        if (sourceTile == null && tile.getActionCommand().equals("")) return;
        
        if (tile == sourceTile) {
            resetSelection();
            return;
        }

        if (!click) {
            String pieceColor = tile.getActionCommand().length() > 5 ? tile.getActionCommand().substring(0, 5) : "";
            if ((gameInit.playerColor == 1 && pieceColor.equals("black")) || 
                (gameInit.playerColor == 0 && pieceColor.equals("white"))) {
                return;
            }

            sourceTile = tile;
            startRowIndex = r;
            startColumnIndex = c;
            click = true;
            
            sourceTile.setBackground(ChessTheme.HIGHLIGHT_SELECTED);

            try {
                possibleMoves = (byte[][]) client.execute("/ChessServer/getPossibleMoves", gameInit.gameId, startRowIndex, startColumnIndex);
                applyHighlights(gameInit.board[startRowIndex][startColumnIndex], possibleMoves);
            } catch (Throwable t) {
                System.err.println("Failed to fetch moves: " + t.getMessage());
                NotificationDialog.showMessage(mainApplicationFrame, "Network Error", "Could not fetch possible moves from server.");
                resetSelection();
            }
        } else {
            destinationRowIndex = r;
            destinationColumnIndex = c;
            click = false;
            targetTile = tile;

            String sourceIconName = sourceTile.getActionCommand();
            
            Move move = new Move();
            move.player = gameInit.playerColor;
            move.piece = gameInit.board[startRowIndex][startColumnIndex];
            move.fromX = startRowIndex;
            move.fromY = startColumnIndex;
            move.toX = destinationRowIndex;
            move.toY = destinationColumnIndex;
            move.castlingType = getCastlingType(move.piece);
            
            PawnPromotionDialog dialog = null;
            if (move.piece == 1 && destinationRowIndex == 0 && possibleMoves[move.toX][move.toY] == 1) {
                dialog = new PawnPromotionDialog(mainApplicationFrame, "white");
                move.pawnPromotionTo = dialog.getSelectedPiece();
            } else if (move.piece == -1 && destinationRowIndex == 7 && possibleMoves[move.toX][move.toY] == 1) {
                dialog = new PawnPromotionDialog(mainApplicationFrame, "black");
                move.pawnPromotionTo = dialog.getSelectedPiece();
            }

            try {
                MoveResponse response = (MoveResponse) client.execute("/ChessServer/submitMove", move, gameInit.gameId);
                if (response.isValid == 0) {
                    resetSelection();
                    return;
                }
                
                move.isLastMove = response.isLastMove;
                move.castlingType = response.castlingType;
                move.pawnPromotionTo = response.pawnPromotionTo;
                move.isInCheck = response.isInCheck;
                move.ambiguityType = response.ambiguityType;
                
                movePiece(sourceIconName);
                handleMoveHistory(move);
                updateBoardState(move);
                
                if (move.pawnPromotionTo != 0 && dialog != null) {
                    applyPromotion(dialog.getPromoteToName(), dialog.getPromoteToIcon());
                }

                if (move.isLastMove == 1) {
                    isOpponentLeftTheGameTimer.stop();
                    SwingUtilities.invokeLater(() -> {
                        mainApplicationFrame.returnToLobbyFromGame();
                        NotificationDialog.showMessage(mainApplicationFrame, "Game Over", "You Won!");
                    });
                    return;
                }

                byte stalemate = (byte) client.execute("/ChessServer/stalemateOccur", gameInit.gameId);
                if (stalemate == 1) {
                    isOpponentLeftTheGameTimer.stop();
                    SwingUtilities.invokeLater(() -> {
                        mainApplicationFrame.returnToLobbyFromGame();
                        NotificationDialog.showMessage(mainApplicationFrame, "Stalemate. The game is a draw", "Game Over");
                    });
                    return;
                }

                canIPlay = false;
                String myColor = gameInit.playerColor == 0 ? "Black" : "White";
                statusLabel.setText("<html><div style='text-align: center;'>Opponent is thinking...<br><span style='font-size:12px; color:#A0A0A0;'>Playing as " + myColor + "</span></div></html>");
                getOpponentMoveTimer.start();
                resetSelection();

            } catch (Throwable t) {
                System.err.println("Failed to submit move: " + t.getMessage());
                NotificationDialog.showMessage(mainApplicationFrame, "Move Failed", "Lost connection to the server. Your move was not sent!");
                resetSelection();
            }
        }
    }

    private void applyHighlights(byte selectedPiece, byte[][] possibleMoves) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (possibleMoves[r][c] == 0) continue;
                
                JButton validTile = tiles[r][c];
                boolean isCapture = (gameInit.board[r][c] != 0);
                boolean isCastling = false;
                
                if (selectedPiece == 6 && startRowIndex == 7 && startColumnIndex == 4 && r == 7 && (c == 2 || c == 6)) isCastling = true;
                if (selectedPiece == -6 && startRowIndex == 0 && startColumnIndex == 4 && r == 0 && (c == 2 || c == 6)) isCastling = true;

                if (isCastling) {
                    validTile.setBorder(ChessTheme.BORDER_CASTLING);
                } else if (isCapture) {
                    validTile.setBorder(ChessTheme.BORDER_CAPTURE);
                } else {
                    validTile.setBorder(ChessTheme.BORDER_MOVE);
                }
            }
        }
    }

    private void resetSelection() {
        click = false;
        sourceTile = null;
        targetTile = null;
        startRowIndex = -1;
        startColumnIndex = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean isLightSquare = (r + c) % 2 == 0;
                tiles[r][c].setBackground(isLightSquare ? ChessTheme.LIGHT_TILE : ChessTheme.DARK_TILE);
                tiles[r][c].setBorder(ChessTheme.EMPTY_BORDER);
                if (possibleMoves != null) possibleMoves[r][c] = 0;
            }
        }
        if (isPlayerTurnHighlighted) highlightTurn();
    }

    private void highlightTurn() {
        isPlayerTurnHighlighted = true;
    }

    private void handleResign() {
        boolean confirm = NotificationDialog.showConfirm(mainApplicationFrame, "Resign", "Are you sure you want to resign?");
        if (confirm) {
            try {
                if (getOpponentMoveTimer != null) getOpponentMoveTimer.stop();
                if (isOpponentLeftTheGameTimer != null) isOpponentLeftTheGameTimer.stop();
                client.execute("/ChessServer/leftGame", username);
                SwingUtilities.invokeLater(() -> mainApplicationFrame.returnToLobbyFromGame());
            } catch (Throwable t) {
                System.err.println("Server unreachable during resign, forcing local exit.");
            } finally {
                SwingUtilities.invokeLater(() -> mainApplicationFrame.returnToLobbyFromGame());
            }
        }
    }

    private byte getCastlingType(byte kingPiece) {
        if (kingPiece == 6 && startRowIndex == 7 && startColumnIndex == 4) {
            if (destinationColumnIndex == 6) return 1;
            if (destinationColumnIndex == 2) return 2;
        } else if (kingPiece == -6 && startRowIndex == 0 && startColumnIndex == 4) {
            if (destinationColumnIndex == 6) return 3;
            if (destinationColumnIndex == 2) return 4;
        }
        return 0;
    }

    private void updateBoardState(Move move) {
        if (move.pawnPromotionTo != 0) {
            gameInit.board[move.fromX][move.fromY] = 0;
            gameInit.board[move.toX][move.toY] = move.pawnPromotionTo;
            return;
        }
        gameInit.board[move.fromX][move.fromY] = 0;
        gameInit.board[move.toX][move.toY] = move.piece;

        if (move.castlingType != 0) {
            byte rX = (move.castlingType <= 2) ? (byte) 7 : (byte) 0;
            byte fY = (move.castlingType == 1 || move.castlingType == 3) ? (byte) 7 : (byte) 0;
            byte tY = (move.castlingType == 1 || move.castlingType == 3) ? (byte) 5 : (byte) 3;

            byte rookPiece = gameInit.board[rX][fY];
            gameInit.board[rX][fY] = 0;
            gameInit.board[rX][tY] = rookPiece;

            this.sourceTile = tiles[rX][fY];
            this.targetTile = tiles[rX][tY];
            movePiece((rookPiece > 0 ? "white" : "black") + "Rook");
        }
    }

    private void movePiece(String sourceIconName) {
        playerPiecesSet.remove(sourceTile);
        playerPiecesSet.remove(targetTile);
        
        String pieceColor = sourceIconName.substring(0, 5);
        if ((gameInit.playerColor == 1 && pieceColor.equals("white")) || (gameInit.playerColor == 0 && pieceColor.equals("black"))) {
            playerPiecesSet.add(targetTile);
        }

        Component[] comps = sourceTile.getComponents();
        if (comps.length > 0) {
            JLabel iconLabel = (JLabel) comps[0];
            sourceTile.removeAll();
            sourceTile.setActionCommand("");
            
            targetTile.removeAll();
            targetTile.setActionCommand(sourceIconName);
            targetTile.setLayout(new BorderLayout());
            targetTile.add(iconLabel);
            
            sourceTile.revalidate(); sourceTile.repaint();
            targetTile.revalidate(); targetTile.repaint();
        }
    }

    private void applyPromotion(String promoteToName, ImageIcon promoteToIcon) {
        JButton pawn = tiles[destinationRowIndex][destinationColumnIndex];
        pawn.removeAll();
        pawn.setActionCommand(promoteToName);
        pawn.setLayout(new BorderLayout());
        pawn.add(new JLabel(promoteToIcon));
        pawn.revalidate();
        pawn.repaint();
    }

    private void handleMoveHistory(Move move) {
        byte isCapture = gameInit.board[move.toX][move.toY] != 0 ? (byte) 1 : 0;
        String pgnMove = PGNConvertor.convertMoveToPGN(move, isCapture);
        if (move.player == 0) moveHistoryPanel.addBlackMove(pgnMove);
        else moveHistoryPanel.addWhiteMove(pgnMove);
    }
}