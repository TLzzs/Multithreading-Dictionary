package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class DictionaryServer {

    private final int port;
    private final String dictionaryFile;
    private final Logger logger;

    private final Map<String, ArrayList<String>> dictionary = new ConcurrentHashMap<>();
    private final Map<String, ArrayList<String>> cache;
    public DictionaryServer(int port, String dictionaryFile, Logger logger) throws IOException {
        this.port = port;
        this.dictionaryFile = dictionaryFile;
        this.logger = logger;

        this.cache = Collections.synchronizedMap(new LinkedHashMap<String, ArrayList<String>>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ArrayList<String>> eldest) {
                return size() > 3;
            }
        });

        loadDictionary();
        startServer();
    }

    private void startServer() throws IOException {
        int numberOfThreads = 50;
        ExecutorService executorService = new ThreadPoolExecutor(0, numberOfThreads,
                60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server stated and listening on port: " + port);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                    executorService.submit(new RequestHandler(clientSocket, this));
                } catch (IOException e) {
                    logger.severe("Exception accepting connection: " + e.getMessage());
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
            logger.severe("Usage: java server.DictionaryServer <port number> <dictionary input>");
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
        synchronized(cache) {
            if (cache.containsKey(word)) {
                logger.info("query from cache");
                return new ArrayList<>(cache.get(word.toLowerCase()));
            }
        }

        logger.info("query from actual dictionary");
        ArrayList<String> meanings = dictionary.get(word.toLowerCase());
        if (meanings!= null && !meanings.isEmpty()) {
            synchronized(cache) {
                cache.put(word, new ArrayList<>(meanings));
            }
        }
        return meanings;
    }

    public boolean isInDictionary (String word) {
        return dictionary.containsKey(word);
    }

    public void addWordAndDefinition(String word , ArrayList<String> definitions) {
        dictionary.put(word, definitions);
        synchronized(cache) {
            cache.put(word, new ArrayList<>(definitions));
        }
    }

    public void removeWordAndDefinition(String word) {
        dictionary.remove(word);
        synchronized(cache) {
            cache.remove(word);
        }
    }

    public void updateDefinition(String word, ArrayList<String> definitions) {
        dictionary.put(word, definitions);
        synchronized(cache) {
            cache.put(word, new ArrayList<>(definitions));
        }
    }

    public boolean isDiffToExisting(String word, ArrayList<String> incoming ) {
        return !new HashSet<>(incoming).equals(new HashSet<>(queryDictionary(word)));
    }
}
