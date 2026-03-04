package io.github.mohammeddaniyal.chess.client;


import io.github.mohammeddaniyal.nframework.client.NFrameworkClient;
import java.io.FileInputStream;
import java.util.Properties;

public class NetworkUtil {
    private static String host = "127.0.0.1";
    private static int port = 5500;
    private static boolean isLoaded = false;

    // This method reads the file ONLY the very first time it is called
    private static void loadConfig() {
        if (isLoaded) return; 
        
        try (FileInputStream input = new FileInputStream("server.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            host = prop.getProperty("HOST", "127.0.0.1");
            port = Integer.parseInt(prop.getProperty("PORT", "5500"));
        } catch (Exception e) {
            System.out.println("No server.properties found, using localhost:5500");
        }
        isLoaded = true;
    }

    // Call this anywhere in your app to get a ready-to-use client!
    public static NFrameworkClient getClient() {
        loadConfig();
        return new NFrameworkClient(host, port);
    }
}
