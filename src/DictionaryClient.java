import Util.ClientUtil;
import GUI.DictionaryClientGui;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class DictionaryClient {
    private final Logger logger;
    private Socket socket;

    public DictionaryClient(String serverAddress, int port, Logger logger) {

        this.logger = logger;

        connectToServer(serverAddress, port);
        createClientGui();

    }

    private void createClientGui() {
        SwingUtilities.invokeLater(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                logger.info("Creating Client GUI...");
                new DictionaryClientGui(logger, new ClientUtil(reader, writer));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void connectToServer(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            logger.info("Successfully connected to server at " + serverAddress + ":" + port);
        } catch (UnknownHostException e) {
            logger.severe("Host could not be determined: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.severe("Error: Unable to establish connection to " + serverAddress + ":" + port + ". " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            logger.severe("Error: Port parameter is outside the specified range of valid port values.");
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(DictionaryClient.class.getName());
        if (args.length < 2) {
            logger.severe("Usage: java DictionaryClient <server address> <port number>");
            return;
        }

        String serverAddress;
        int port;
        try {
            serverAddress = args[0];
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            logger.severe("Error: Invalid port number provided.");
            return;
        }

        new DictionaryClient(serverAddress, port, logger);
    }
}
