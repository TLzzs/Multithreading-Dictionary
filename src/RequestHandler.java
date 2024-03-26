import Mapper.Mapper;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

import static Config.CommunicateConfig.*;

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
        String[] parts = request.split(COLON, 3);
        String action = parts[0];
        String response = switch (action.toUpperCase()) {
            case QUERY -> handleQuery(parts[1]);
            case ADD -> handleAdd(parts[1], parts[2]);
            case REMOVE -> handleRemove(parts[1]);
            case UPDATE -> handleUpdate(parts[1], parts[2]);
            default -> "Unknown action." + END_OF_LINE;
        };

        writer.write(response);
        writer.write(FINISH_FROM_SERVER);
        writer.newLine();
        writer.flush();
    }

    private String handleUpdate(String word, String definitions) {
        logger.info("received word: "+ word);
        logger.info("received definition: " + definitions);

        ArrayList<String> definitionList = Mapper.convertStringToArrayList(definitions, ",");
        if (dictionaryServer.isInDictionary(word)) {
            dictionaryServer.updateDefinition(word, definitionList);
            logger.info( "Successfully update word " + word);
            return "Successfully update word " + word +" and its definitions" + END_OF_LINE;

        } else {
            return WORD_NOT_FOUND_FROM_SERVER + END_OF_LINE;
        }
    }

    private String handleRemove(String word) {
        logger.info("received word: "+ word);
        if (dictionaryServer.isInDictionary(word)) {
            dictionaryServer.removeWordAndDefinition(word);
            return "Successfully remove word " + word +  " from Dictionary..." + END_OF_LINE;
        } else {
            return "This word was not existing in dictionary , please retry..." + END_OF_LINE;
        }
    }

    private String handleAdd(String word, String definitions) {
        logger.info("received word: "+ word);
        logger.info("received definition: " + definitions);
        ArrayList<String> definitionList = Mapper.convertStringToArrayList(definitions, ",");
        if (!dictionaryServer.isInDictionary(word)) {
            dictionaryServer.addWordAndDefinition(word, definitionList);
            return "Successfully Added to Dictionary..." + END_OF_LINE;
        } else {
            return "This word was in dictionary , please update if required..." + END_OF_LINE;
        }
    }

    private String handleQuery(String word) {
        logger.info("Searching for word: " + word);
        ArrayList<String> definitions = dictionaryServer.queryDictionary(word);

        if (definitions != null) {
            StringBuilder response = new StringBuilder();
            for (String definition : definitions) {
                response.append(definition).append(END_OF_LINE);
            }
            logger.info("server found: "+ word);
            return response.toString();
        } else {
            return WORD_NOT_FOUND_FROM_SERVER + END_OF_LINE;
        }
    }
}
