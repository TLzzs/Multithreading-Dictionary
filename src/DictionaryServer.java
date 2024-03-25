import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class DictionaryServer {

    private final int port;
    private final String dictionaryFile;
    private final Logger logger;

    private final Map<String, ArrayList<String>> dictionary = new ConcurrentHashMap<>();
    public DictionaryServer(int port, String dictionaryFile, Logger logger) throws IOException {
        this.port = port;
        this.dictionaryFile = dictionaryFile;
        this.logger = logger;
        
        loadDictionary();
        startServer();
    }

    private void startServer() throws IOException {
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server stated and listening on port: " + port);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                    executorService.submit(new RequestHandler(clientSocket, this));
                } catch (IOException e) {
                    System.err.println("Exception accepting connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } finally {
            executorService.shutdown();
        }
    }

    private void loadDictionary() {
        logger.info("Starting load dictionary to server...");
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dictionaryFile)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String word = parts[0].trim();
                        String[] meaningsArray = parts[1].split(";");
                        ArrayList<String> meaningsList = new ArrayList<>();
                        for (String meaning : meaningsArray) {
                            if (!meaning.trim().isEmpty()) {
                                meaningsList.add(meaning.trim());
                            }
                        }
                        dictionary.put(word.toLowerCase(), meaningsList);
                    }
                }
                logger.info("Dictionary has been loaded to server...");
            }
        } catch (IOException e) {
            logger.severe("Error loading dictionary from file " + dictionaryFile + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        Logger logger = Logger.getLogger(DictionaryServer.class.getName());
        if (args.length < 2) {
            logger.severe("Usage: java DictionaryServer <port number> <dictionary input>");
            return;
        }
        int port;
        String dictionaryFile;
        try {
            port = Integer.parseInt(args[0]);
            dictionaryFile = args[1];
        } catch  (NumberFormatException e) {
            logger.severe("Error: Invalid port number provided.");
            return;
        }

        new DictionaryServer(port, dictionaryFile, logger);
    }


    public ArrayList<String> queryDictionary(String word) {
        return dictionary.get(word.toLowerCase());
    }

    public boolean isInDictionary (String word) {
        return dictionary.containsKey(word);
    }

    public void addWordAndDefinition(String word , ArrayList<String> definitions) {
        dictionary.put(word, definitions);
    }

    public void removeWordAndDefinition(String word) {
        dictionary.remove(word);
    }
}
