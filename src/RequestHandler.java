import Mapper.Mapper;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private final DictionaryServer dictionaryServer;
    private final Logger logger = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket clientSocket, DictionaryServer dictionaryServer) {
        this.clientSocket = clientSocket;
        this.dictionaryServer = dictionaryServer;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            String request;
            while ((request = reader.readLine()) != null) {
                processRequest(request, writer);
            }
        } catch (IOException e) {
            logger.severe("Error in client handler: " + e.getMessage());
        } finally {
            closeSocket();
        }
    }

    private void closeSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.severe("Error closing socket: " + e.getMessage());
        }
    }

    private void processRequest(String request, BufferedWriter writer) throws IOException {
        String[] parts = request.split(":", 3);
        String action = parts[0];
        String response = switch (action.toUpperCase()) {
            case "QUERY" -> handleQuery(parts[1]);
            case "ADD" -> handleAdd(parts[1], parts[2]);
            default -> "Unknown action.\n";
        };

        writer.write(response);
        writer.write("FINISH FROM SERVER\n");
        writer.flush();
    }

    private String handleAdd(String word, String definitions) {
        logger.info("received word: "+ word);
        logger.info("received definition: " + definitions);
        ArrayList<String> definitionList = Mapper.convertStringToArrayList(definitions);
        if (!dictionaryServer.isInDictionary(word)) {
            dictionaryServer.addWordAndDefinition(word, definitionList);
            return "Successfully Added to Dictionary...\n";
        } else {
            return "This word was in dictionary , please update if required...\n";
        }
    }

    private String handleQuery(String word) {
        logger.info("Searching for word: " + word);
        ArrayList<String> definitions = dictionaryServer.queryDictionary(word);

        if (definitions != null) {
            StringBuilder response = new StringBuilder();
            for (int i = 0; i < definitions.size(); i++) {
                response.append("Definition ").append(i + 1).append(": ").append(definitions.get(i)).append('\n');
            }
            return response.toString();
        } else {
            return "Word not found.\n";
        }
    }
}
