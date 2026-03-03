package io.github.mohammeddaniyal.chess.server;
import io.github.mohammeddaniyal.nframework.server.*;
import io.github.mohammeddaniyal.chess.server.utils.ChessLogger; 

public class Main {
    public static void main(String args[]) {
        // 1. Start the logging engine first
        ChessLogger.initialize();
        ChessLogger.log.info("Attempting to start Chess Server...");

        try {
            NFrameworkServer server = new NFrameworkServer();
            server.registerClass(ChessServer.class);
            server.start();
            ChessLogger.log.info("Chess Server successfully started and listening for clients.");
        } catch (Throwable t) {
            // If the server crashes on startup, log the exact reason
            ChessLogger.log.severe("CRITICAL: Server failed to start - " + t.getMessage());
            t.printStackTrace();
        }
    }
}