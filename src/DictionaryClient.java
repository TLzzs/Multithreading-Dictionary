import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class DictionaryClient {
    private final Logger logger;
    private Socket socket;
    private final String serverAddress;
    private final int port;

    public DictionaryClient(String serverAddress, int port, Logger logger) {

        this.logger = logger;
        this.serverAddress = serverAddress;
        this.port = port;
        connectToServer(serverAddress, port);
        createClientGui();

    }

    private void createClientGui() {
        SwingUtilities.invokeLater(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                logger.info("Creating Client GUI...");
                new DictionaryClientGui(logger, new RequestSendingHandler(reader, writer));
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
            handleReconnection();
        } catch (IllegalArgumentException e) {
            logger.severe("Error: Port parameter is outside the specified range of valid port values.");
            throw new RuntimeException(e);
        }


    }

    private void handleReconnection() {
        int attempt = 0;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                logger.info("Attempting to reconnect to the server...");
                Thread.sleep(Math.min(1000 * (1L << attempt), 30000));
                attempt++;
                socket = new Socket(serverAddress, port);
                logger.info("Reconnected to the server.");
                break;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                logger.severe("Reconnection attempt interrupted.");
                return;
            } catch (IOException e) {
                logger.info("Reconnection attempt failed. Trying again...");
            }
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
