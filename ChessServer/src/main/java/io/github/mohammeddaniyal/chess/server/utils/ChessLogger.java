package io.github.mohammeddaniyal.chess.server.utils;

import java.util.logging.*;
import java.io.IOException;

public class ChessLogger {
    // A single, globally accessible logger
    public static final Logger log = Logger.getLogger("ChessServerLog");

    public static void initialize() {
        log.setUseParentHandlers(false);
        try {
            // Write logs to "server_logs.txt" AND keep the console active
            FileHandler fileHandler = new FileHandler("server_logs.txt", true);
            fileHandler.setFormatter(new SimpleFormatter()); 
            log.addHandler(fileHandler);
            
            // Console formatting
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            log.addHandler(consoleHandler);

            log.info("--- CHESS SERVER LOGGING INITIALIZED ---");
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to initialize logger: " + e.getMessage());
        }
    }
}