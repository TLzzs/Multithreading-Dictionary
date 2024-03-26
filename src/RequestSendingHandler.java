import Mapper.Mapper;
import Config.CommunicateConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static Config.CommunicateConfig.*;
import static Mapper.Mapper.incomingMeaningToServer;

public class RequestSendingHandler {
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private final Logger logger = Logger.getLogger(RequestSendingHandler.class.getName());

    public RequestSendingHandler(BufferedReader reader, BufferedWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public String queryServer(String word) {
        try {
            logger.info("start querying process towards server ");
            writer.write(CommunicateConfig.QUERY + COLON + word);
            writer.newLine();
            writer.flush();

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty() && !line.equals("FINISH FROM SERVER")) {
                response.append(line).append(END_OF_LINE);
            }
            return response.toString();
        } catch (IOException e) {
            logger.severe("Error communicating with the server: " + e.getMessage());
            return "Error querying the server.";
        }
    }

    public String sendAddToServer(String word, List<String> meanings) {
        try {
            logger.info("requesting add a new word into dictionary towards server ");

            writer.write(ADD + COLON + word.toLowerCase() + COLON + incomingMeaningToServer(meanings));
            writer.newLine();
            writer.flush();

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty() && !line.equals(FINISH_FROM_SERVER)) {
                if (line.equals(WORD_NOT_FOUND_FROM_SERVER)) {
                    response.append("NOT FOUND IN DICTIONARY, PLEASE TRY ANOTHER ONE").append(END_OF_LINE);
                } else {
                    response.append(line).append(END_OF_LINE);
                }
            }
            return response.toString();
        } catch (IOException e) {
            logger.severe("Error communicating with the server: " + e.getMessage());
            return "An Unexpected Error Occurs , please restart the application";
        }
    }

    public String removeFromServer(String word) {
        try {
            logger.info("requesting remove a word from dictionary");
            writer.write(REMOVE + COLON + word.toLowerCase());
            writer.newLine();
            writer.flush();

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty() && !line.equals(FINISH_FROM_SERVER)) {
                logger.info(line);
                response.append(line).append(END_OF_LINE);
            }
            return response.toString();
        } catch (IOException e) {
            logger.severe("Error communicating with the server: " + e.getMessage());
            return "An Unexpected Error Occurs , please restart the application";
        }
    }

    public List<String> fetchDefinitions(String word) {
        logger.info("fetch definition from query server");
        return Mapper.convertStringToArrayList(queryServer(word), END_OF_LINE, false);
    }

    public String updateDictionary(String word, List<String> meanings) {
        try {
            logger.info("updating the definition in server");

            writer.write(UPDATE + COLON + word.toLowerCase() + COLON + incomingMeaningToServer(meanings));
            writer.newLine();
            writer.flush();

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty() && !line.equals(FINISH_FROM_SERVER)) {
                if (line.equals(WORD_NOT_FOUND_FROM_SERVER)) {
                    response.append("This word is not in dictionary")
                            .append(END_OF_LINE)
                            .append("Tips: ")
                            .append("might be deleted by someone else just now, please add it first")
                            .append(END_OF_LINE);

                } else {
                    response.append(line).append(END_OF_LINE);
                }

            }
            return response.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
