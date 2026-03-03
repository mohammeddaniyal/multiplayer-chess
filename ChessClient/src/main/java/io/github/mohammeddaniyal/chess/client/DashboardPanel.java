package io.github.mohammeddaniyal.chess.client;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.LinkedList;

import io.github.mohammeddaniyal.nframework.client.NFrameworkClient;
import io.github.mohammeddaniyal.chess.client.logic.NotificationDialog;
import io.github.mohammeddaniyal.chess.common.*;

public class DashboardPanel extends JPanel {

    private JTable availableMembersTable;
    private JTable invitationsTable;
    private JLabel welcomeLabel;
    private NFrameworkClient client;
    private String username;

    private Timer membersTimer;
    private Timer invitationsTimer;
    private Timer invitationsClearUpTimer;

    private AvailableMembersListModel availableMembersListModel;
    private InvitationsListModel invitationsListModel;

    private final Color BACKGROUND_COLOR = new Color(43, 43, 43);
    private final Color PANEL_BG = new Color(60, 63, 65);
    private final Color TEXT_COLOR = new Color(220, 220, 220);
    private final Color HEADER_BG = new Color(30, 30, 30);
    private final Color ACCENT_COLOR = new Color(72, 118, 168);

    public DashboardPanel(ActionListener onLogoutAction) {
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);

        welcomeLabel = new JLabel("Lobby - Welcome");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        welcomeLabel.setForeground(TEXT_COLOR);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutButton.setBackground(new Color(200, 70, 70));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        logoutButton.setPreferredSize(new Dimension(120, 35));
        logoutButton.addActionListener(onLogoutAction);

        headerPanel.add(logoutButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Lobby Content: Two columns for players and invites
        JPanel tablesContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesContainer.setBackground(BACKGROUND_COLOR);

        // Players Table setup
        availableMembersListModel = new AvailableMembersListModel();
        availableMembersTable = new JTable(availableMembersListModel);
        availableMembersTable.getColumn("Status").setCellRenderer(new AvailableMemberListStatusRenderer());
        availableMembersTable.getColumn("Action").setCellRenderer(new AvailableMembersListButtonRenderer());
        availableMembersTable.getColumn("Action")
                .setCellEditor(new AvailableMembersListButtonCellEditor(availableMembersListModel));

        // Invitations Table setup
        invitationsListModel = new InvitationsListModel();
        invitationsTable = new JTable(invitationsListModel);
        invitationsTable.getColumn("Accept").setCellRenderer(new InvitationsListButtonRenderer());
        invitationsTable.getColumn("Accept").setCellEditor(new InvitationsListButtonCellEditor(invitationsListModel));
        invitationsTable.getColumn("Reject").setCellRenderer(new InvitationsListButtonRenderer());
        invitationsTable.getColumn("Reject").setCellEditor(new InvitationsListButtonCellEditor(invitationsListModel));

        tablesContainer.add(createStyledTablePanel("Available Players", availableMembersTable));
        tablesContainer.add(createStyledTablePanel("Game Invitations", invitationsTable));

        add(tablesContainer, BorderLayout.CENTER);
    }

    private JPanel createStyledTablePanel(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Flat dark theme style
        table.setBackground(PANEL_BG);
        table.setForeground(TEXT_COLOR);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(85, 88, 90));

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(85, 85, 85), 1));
        scrollPane.getViewport().setBackground(PANEL_BG);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public void setUsername(String username) {
        this.username = username;
        welcomeLabel.setText("Lobby - Welcome, " + username);
    }

    public void startNetworking(NFrameworkClient client, String username) {
        this.client = client;
        this.username = username;
        welcomeLabel.setText("Lobby - Welcome, " + username);

        membersTimer = new Timer(1000, e -> fetchAvailableMembers());
        membersTimer.start();

        invitationsTimer = new Timer(1000, e -> fetchMessages());
        invitationsTimer.start();

        invitationsClearUpTimer = new Timer(1000, e -> clearExpiredInvitations());
    }

    public void stopNetworking() {
        if (membersTimer != null)
            membersTimer.stop();
        if (invitationsTimer != null)
            invitationsTimer.stop();
        if (invitationsClearUpTimer != null)
            invitationsClearUpTimer.stop();
    }

    public void clearData() {
        availableMembersListModel.setMembers(new LinkedList<>());
        invitationsListModel.setMessages(new LinkedList<>());
        welcomeLabel.setText("Lobby");
    }

    public void resetInviteState() {
        if (availableMembersListModel != null) {
            SwingUtilities.invokeLater(() -> {
                availableMembersListModel.enableInviteButtons();
            });
        }
    }

    private void fetchMessages() {
        invitationsTimer.stop();
        try {
            List<Message> messages = (List<Message>) client.execute("/ChessServer/getMessages", username);

            if (messages != null && !messages.isEmpty()) {
                invitationsClearUpTimer.start();
                for (Message message : messages) {
                    if (message.type == MESSAGE_TYPE.CHALLENGE_ACCEPTED) {
                        // Handled by MainApplicationFrame
                    } else if (message.type == MESSAGE_TYPE.CHALLENGE_REJECTED) {
                        SwingUtilities.invokeLater(() -> availableMembersListModel.enableInviteButtons());
                    }
                }
                SwingUtilities.invokeLater(() -> invitationsListModel.setMessages(messages));
            }
        } catch (Throwable t) {
            System.err.println("Polling error (messages): " + t.getMessage());
        } finally {
            invitationsTimer.start();
        }
    }

    private void clearExpiredInvitations() {
        try {
            List<String> expiredUsers = (List<String>) client.execute("/ChessServer/expiredInvitations", username);
            if (expiredUsers != null && !expiredUsers.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    for (String user : expiredUsers)
                        invitationsListModel.removeInvitationOfUser(user);
                });
            }
        } catch (Throwable t) {
            System.err.println("Cleanup error: " + t.getMessage());
        }
    }

    private void fetchAvailableMembers() {
        membersTimer.stop();
        try {
            List<MemberInfo> members = (List<MemberInfo>) client.execute("/ChessServer/getMembers", username);
            SwingUtilities.invokeLater(() -> availableMembersListModel.setMembers(members));
        } catch (Throwable t) {
            System.err.println("Polling error (members): " + t.getMessage());
        } finally {
            membersTimer.start();
        }
    }

    public void sendInvitation(String toUsername) {
        try {
            client.execute("/ChessServer/inviteUser", username, toUsername);

            Timer statusCheck = new Timer(1000, ev -> {
                try {
                    Message msg = (Message) client.execute("/ChessServer/getInvitationStatus", username, toUsername);
                    if (msg != null && (msg.type == MESSAGE_TYPE.CHALLENGE_IGNORED
                            || msg.type == MESSAGE_TYPE.CHALLENGE_REJECTED)) {
                        ((Timer) ev.getSource()).stop();
                        SwingUtilities.invokeLater(() -> availableMembersListModel.enableInviteButtons());
                    }
                } catch (Throwable t) {
                    /* retry */ }
            });
            statusCheck.start();

        } catch (Throwable t) {
            NotificationDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "Network Error",
                    "Failed to send invitation.");
            availableMembersListModel.enableInviteButtons();
        }
    }

    public void sendInvitationReply(Message message) {
        try {
            client.execute("/ChessServer/invitationReply", message);
        } catch (Throwable t) {
            NotificationDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "Network Error",
                    "Failed to send reply.");
        }
    }

    // --- Inner Classes for Table Handling ---

    class AvailableMembersListModel extends AbstractTableModel {
        private List<String> members = new LinkedList<>();
        private List<PLAYER_STATUS_TYPE> status = new LinkedList<>();
        private List<JButton> inviteButtons = new LinkedList<>();
        private String[] titles = { "Member", "Status", "Action" };
        private boolean awaitingReply = false;

        public int getRowCount() {
            return members.size();
        }

        public int getColumnCount() {
            return titles.length;
        }

        public String getColumnName(int column) {
            return titles[column];
        }

        public Object getValueAt(int row, int col) {
            if (col == 0)
                return members.get(row);
            if (col == 1)
                return status.get(row);
            return inviteButtons.get(row);
        }

        public boolean isCellEditable(int row, int col) {
            return col == 2;
        }

        public Class<?> getColumnClass(int col) {
            if (col == 0)
                return String.class;
            if (col == 1)
                return PLAYER_STATUS_TYPE.class;
            return JButton.class;
        }

        public void setMembers(List<MemberInfo> newMembers) {
            if (awaitingReply)
                return;
            members.clear();
            status.clear();
            inviteButtons.clear();
            for (MemberInfo info : newMembers) {
                members.add(info.member);
                status.add(info.status);
                JButton btn = new JButton("Invite");
                btn.setBackground(ACCENT_COLOR);
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                if (info.status != PLAYER_STATUS_TYPE.ONLINE) {
                    btn.setEnabled(false);
                    btn.setBackground(Color.GRAY);
                }
                inviteButtons.add(btn);
            }
            fireTableDataChanged();
        }

        public void setValueAt(Object data, int row, int col) {
            if (col == 2) {
                awaitingReply = true;
                for (JButton b : inviteButtons)
                    b.setEnabled(false);
                fireTableDataChanged();
                sendInvitation(members.get(row));
            }
        }

        public void enableInviteButtons() {
            awaitingReply = false;
            fireTableDataChanged();
        }
    }

    class AvailableMemberListStatusRenderer implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int col) {
            JLabel label = new JLabel("\u25CF");
            label.setFont(new Font("Arial", Font.PLAIN, 18));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            Color c = Color.GRAY;
            if (value == PLAYER_STATUS_TYPE.ONLINE)
                c = new Color(85, 190, 85);
            else if (value == PLAYER_STATUS_TYPE.OFFLINE)
                c = new Color(220, 80, 80);
            else if (value == PLAYER_STATUS_TYPE.IN_GAME)
                c = new Color(220, 180, 50);
            label.setForeground(c);
            label.setOpaque(true);
            label.setBackground(isSelected ? table.getSelectionBackground() : PANEL_BG);
            return label;
        }
    }

    class AvailableMembersListButtonRenderer implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int col) {
            return (JButton) value;
        }
    }

    class AvailableMembersListButtonCellEditor extends DefaultCellEditor {
        private AvailableMembersListModel model;

        AvailableMembersListButtonCellEditor(AvailableMembersListModel model) {
            super(new JCheckBox());
            this.model = model;
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            JButton btn = (JButton) model.getValueAt(row, col);
            btn.addActionListener(e -> fireEditingStopped());
            return btn;
        }

        public Object getCellEditorValue() {
            return "Invited";
        }
    }

    class InvitationsListModel extends AbstractTableModel {
        private List<String> members = new LinkedList<>();
        private List<JButton> acceptButtons = new LinkedList<>();
        private List<JButton> rejectButtons = new LinkedList<>();
        private String[] titles = { "Challenger", "Accept", "Reject" };

        public int getRowCount() {
            return members.size();
        }

        public int getColumnCount() {
            return titles.length;
        }

        public String getColumnName(int column) {
            return titles[column];
        }

        public Object getValueAt(int row, int col) {
            if (col == 0)
                return members.get(row);
            if (col == 1)
                return acceptButtons.get(row);
            return rejectButtons.get(row);
        }

        public boolean isCellEditable(int row, int col) {
            return col > 0;
        }

        public Class<?> getColumnClass(int col) {
            return col == 0 ? String.class : JButton.class;
        }

        public void setMessages(List<Message> messages) {
            if (messages == null)
                return;
            for (Message m : messages) {
                if (m.type == MESSAGE_TYPE.CHALLENGE && !members.contains(m.fromUsername)) {
                    members.add(m.fromUsername);
                    JButton accept = new JButton("Accept");
                    accept.setBackground(new Color(60, 150, 80));
                    accept.setForeground(Color.WHITE);
                    acceptButtons.add(accept);

                    JButton reject = new JButton("Reject");
                    reject.setBackground(new Color(200, 70, 70));
                    reject.setForeground(Color.WHITE);
                    rejectButtons.add(reject);
                }
            }
            fireTableDataChanged();
        }

        public void setValueAt(Object data, int row, int col) {
            Message msg = new Message();
            msg.fromUsername = username;
            msg.toUsername = members.get(row);
            msg.type = data.equals("Accepted") ? MESSAGE_TYPE.CHALLENGE_ACCEPTED : MESSAGE_TYPE.CHALLENGE_REJECTED;

            sendInvitationReply(msg);
            members.remove(row);
            acceptButtons.remove(row);
            rejectButtons.remove(row);
            fireTableDataChanged();
        }

        public void removeInvitationOfUser(String user) {
            int row = members.indexOf(user);
            if (row != -1) {
                members.remove(row);
                acceptButtons.remove(row);
                rejectButtons.remove(row);
                fireTableDataChanged();
            }
        }
    }

    class InvitationsListButtonRenderer implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int col) {
            return (JButton) value;
        }
    }

    class InvitationsListButtonCellEditor extends DefaultCellEditor {
        private String lastValue;

        InvitationsListButtonCellEditor(InvitationsListModel model) {
            super(new JCheckBox());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            JButton btn = (JButton) value;
            btn.addActionListener(e -> fireEditingStopped());
            lastValue = col == 1 ? "Accepted" : "Rejected";
            return btn;
        }

        public Object getCellEditorValue() {
            return lastValue;
        }
    }
}