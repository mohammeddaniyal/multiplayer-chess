package io.github.mohammeddaniyal.chess.server;

import io.github.mohammeddaniyal.nframework.server.*;

import java.io.FileInputStream;
import java.util.Properties;

import io.github.mohammeddaniyal.chess.server.utils.ChessLogger;

public class Main {
    public static void main(String args[]) {
        // 1. Start the logging engine first
        ChessLogger.initialize();
        ChessLogger.log.info("Attempting to start Chess Server...");
        int port = 5500; // Default fallback
        try (FileInputStream input = new FileInputStream("server.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            port = Integer.parseInt(prop.getProperty("PORT", "5500"));
            ChessLogger.log.info("Loaded config. Port: " + port);
        } catch (Exception e) {
            ChessLogger.log.warning("server.properties not found. Defaulting to port 5500.");
        }

        try {
            NFrameworkServer server = new NFrameworkServer(port);
            server.registerClass(ChessServer.class);
            server.start();
            ChessLogger.log.info("Chess Server successfully started and listening on port " + port);
        } catch (Throwable t) {
            // If the server crashes on startup, log the exact reason
            ChessLogger.log.severe("CRITICAL: Server failed to start - " + t.getMessage());
            t.printStackTrace();
        }
    }
}