package io.github.mohammeddaniyal.chess.client;

import javax.swing.SwingUtilities;
import io.github.mohammeddaniyal.chess.client.logic.NotificationDialog;

public class Main {
    public static void main(String args[]) {
        // Global handler to catch any thread-level exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Critical Failure: " + throwable.getMessage());
            throwable.printStackTrace();

            SwingUtilities.invokeLater(() -> {
                NotificationDialog.showMessage(null, "Application Error", 
                    "An unexpected error occurred. The application may need to be restarted.");
            });
        });

        // Start the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainApplicationFrame frame = new MainApplicationFrame();
            frame.setVisible(true);
        });
    }
}