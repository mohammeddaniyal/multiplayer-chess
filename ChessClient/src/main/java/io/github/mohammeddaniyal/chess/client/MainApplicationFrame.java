package io.github.mohammeddaniyal.chess.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Timer;

import io.github.mohammeddaniyal.nframework.client.NFrameworkClient;
import io.github.mohammeddaniyal.chess.client.logic.Chess;
import io.github.mohammeddaniyal.chess.client.logic.ChessTheme;
import io.github.mohammeddaniyal.chess.client.logic.NotificationDialog;
import io.github.mohammeddaniyal.chess.common.GameInit;

public class MainApplicationFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;

    public static final String LOGIN_SCREEN = "LOGIN";
    public static final String DASHBOARD_SCREEN = "DASHBOARD";
    public static final String GAME_SCREEN = "GAME";

    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private JPanel gameWrapperPanel;

    private Timer gameInitTimer;
    private NFrameworkClient client;
    private String username;

    private boolean isCleanlyExiting = false;

    public MainApplicationFrame() {
        super("Chess Engine");

        try {
            ImageIcon rawIcon = ChessTheme.loadIcon("logo.png");
            // scale to standard 64x64 icon size
            Image scaledIcon = rawIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            setIconImage(scaledIcon);
        } catch (Exception e) {
            System.err.println("App icon load failed.");
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        client = new NFrameworkClient();
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        loginPanel = new LoginPanel(e -> handleLoginAttempt());
        dashboardPanel = new DashboardPanel(e -> handleLogoutAttempt());

        gameWrapperPanel = new JPanel(new BorderLayout());
        gameWrapperPanel.setBackground(new Color(43, 43, 43));

        mainContainer.add(loginPanel, LOGIN_SCREEN);
        mainContainer.add(dashboardPanel, DASHBOARD_SCREEN);
        mainContainer.add(gameWrapperPanel, GAME_SCREEN);

        add(mainContainer);
        cardLayout.show(mainContainer, LOGIN_SCREEN);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                handleAppClosing();
            }
        });

        // cleanup server session on JVM shutdown (Ctrl+C / unexpected kill)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (this.username != null && !isCleanlyExiting) {
                try {
                    client.execute("/ChessServer/leftGame", this.username);
                    client.execute("/ChessServer/logout", this.username);
                } catch (Throwable t) {
                    // ignore during shutdown
                }
            }
        }));
    }

    private void handleAppClosing() {
        isCleanlyExiting = true;
        if (this.username == null) {
            System.exit(0);
            return;
        }

        boolean isInGame = (gameWrapperPanel.getComponentCount() > 0);

        if (isInGame) {
            if (NotificationDialog.showConfirm(this, "Resign & Exit", "Are you sure you want to resign and exit?")) {
                try {
                    client.execute("/ChessServer/leftGame", this.username);
                    client.execute("/ChessServer/logout", this.username);
                } catch (Throwable t) {
                    System.err.println("Exit error: " + t.getMessage());
                } finally {
                    System.exit(0);
                }
            }
        } else {
            if (NotificationDialog.showConfirm(this, "Exit Chess", "Are you sure you want to exit the application?")) {
                try {
                    client.execute("/ChessServer/logout", this.username);
                } catch (Throwable t) {
                    System.err.println("Logout error: " + t.getMessage());
                } finally {
                    System.exit(0);
                }
            }
        }
    }

    private void handleLoginAttempt() {
        String inputUsername = loginPanel.getUsername();
        String inputPassword = loginPanel.getPassword();

        if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
            loginPanel.showError("Username and Password are required.");
            return;
        }

        try {
            boolean authentic = (Boolean) client.execute("/ChessServer/authenticateMember", inputUsername,
                    inputPassword);
            if (authentic) {
                this.username = inputUsername;
                dashboardPanel.startNetworking(this.client, this.username);
                startGameInitPoller();
                navigateToDashboard();
            } else {
                loginPanel.showError("Invalid credentials.");
            }
        } catch (Throwable t) {
            loginPanel.showError("");
            NotificationDialog.showMessage(this, "Server Error", "Could not connect to the chess server.");
        }
    }

    private void handleLogoutAttempt() {
        try {
            dashboardPanel.stopNetworking();
            if (gameInitTimer != null && gameInitTimer.isRunning()) {
                gameInitTimer.stop();
            }
            client.execute("/ChessServer/logout", this.username);
        } catch (Throwable t) {
            System.err.println("Logout failed: " + t.getMessage());
        } finally {
            dashboardPanel.clearData();
            loginPanel.resetFields();
            this.username = null;
            navigateToLogin();
        }
    }

    private void startGameInitPoller() {
        if (gameInitTimer != null && gameInitTimer.isRunning())
            return;

        gameInitTimer = new Timer(1000, e -> {
            try {
                GameInit gameInit = (GameInit) client.execute("/ChessServer/getGameInit", this.username);
                if (gameInit != null) {
                    gameInitTimer.stop();
                    dashboardPanel.stopNetworking();
                    navigateToGame();
                    startCountdown(gameInit);
                }
            } catch (Throwable t) {
                System.err.println("Matchmaking poll blip...");
            }
        });
        gameInitTimer.start();
    }

    private void startCountdown(GameInit gameInit) {
        gameWrapperPanel.removeAll();

        JLabel countdownLabel = new JLabel("Game starting in: 5", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 36));
        countdownLabel.setForeground(Color.RED);
        gameWrapperPanel.add(countdownLabel, BorderLayout.CENTER);

        gameWrapperPanel.revalidate();
        gameWrapperPanel.repaint();

        Timer countdownTimer = new Timer(1000, new ActionListener() {
            int counter = 4;

            @Override
            public void actionPerformed(ActionEvent ev) {
                if (counter > 0) {
                    countdownLabel.setText("Game starting in: " + counter--);
                } else {
                    ((Timer) ev.getSource()).stop();
                    gameWrapperPanel.removeAll();

                    Chess chessPanel = new Chess(MainApplicationFrame.this, client, gameInit, username);
                    gameWrapperPanel.add(chessPanel, BorderLayout.CENTER);

                    gameWrapperPanel.revalidate();
                    gameWrapperPanel.repaint();
                }
            }
        });
        countdownTimer.start();
    }

    public void returnToLobbyFromGame() {
        gameWrapperPanel.removeAll();
        dashboardPanel.resetInviteState();
        dashboardPanel.startNetworking(this.client, this.username);
        startGameInitPoller();
        navigateToDashboard();
    }

    public void navigateToDashboard() {
        cardLayout.show(mainContainer, DASHBOARD_SCREEN);
    }

    public void navigateToGame() {
        cardLayout.show(mainContainer, GAME_SCREEN);
    }

    public void navigateToLogin() {
        cardLayout.show(mainContainer, LOGIN_SCREEN);
    }
}