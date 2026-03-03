package io.github.mohammeddaniyal.chess.client.history;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class MoveHistoryPanel extends JPanel {
    private MoveHistoryTableModel moveHistoryTableModel;
    private JTable moveHistoryTable;
    private JScrollPane moveHistoryTableScrollPane;

    // Dark theme palette
    private final Color HEADER_BG = new Color(40, 40, 40);
    private final Color HEADER_FG = new Color(240, 240, 240);
    private final Color TABLE_BG = new Color(60, 63, 65);
    private final Color TABLE_FG = new Color(220, 220, 220);

    public MoveHistoryPanel(byte firstPlayerColor) {
        moveHistoryTableModel = new MoveHistoryTableModel(firstPlayerColor);
        moveHistoryTable = new JTable(moveHistoryTableModel);

        moveHistoryTable.getTableHeader().setResizingAllowed(false);
        moveHistoryTable.getTableHeader().setReorderingAllowed(false);

        // pad the text so it isn't flush against the cell edges
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); 
        
        moveHistoryTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        moveHistoryTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);

        moveHistoryTable.setBackground(TABLE_BG);
        moveHistoryTable.setForeground(TABLE_FG);
        moveHistoryTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        moveHistoryTable.setRowHeight(25);
        moveHistoryTable.setShowGrid(false); // hide default spreadsheet grid
        moveHistoryTable.setIntercellSpacing(new Dimension(0, 0));
        moveHistoryTable.setSelectionBackground(new Color(85, 88, 90));
        
        JTableHeader header = moveHistoryTable.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_FG);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(100, 30));
        
        moveHistoryTableScrollPane = new JScrollPane(moveHistoryTable);
        moveHistoryTableScrollPane.setBorder(BorderFactory.createEmptyBorder()); // flatten scrollpane 
        moveHistoryTableScrollPane.getViewport().setBackground(TABLE_BG); 

        setLayout(new BorderLayout());
        setBackground(TABLE_BG);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        
        // constrain width, let height flex
        setPreferredSize(new Dimension(250, 0)); 
        
        add(moveHistoryTableScrollPane, BorderLayout.CENTER);
    }

    public void addBlackMove(String move) {
        moveHistoryTableModel.addBlackMove(move);
        scrollToBottom();
    }

    public void addWhiteMove(String move) {
        moveHistoryTableModel.addWhiteMove(move);
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        int rowCount = moveHistoryTable.getRowCount();
        if (rowCount > 0) {
            moveHistoryTable.scrollRectToVisible(moveHistoryTable.getCellRect(rowCount - 1, 0, true));
        }
    }
}