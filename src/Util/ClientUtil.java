package Util;

import Mapper.Mapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static Util.CommunicateConfig.*;

public class ClientUtil {
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private final Logger logger = Logger.getLogger(ClientUtil.class.getName());

    public ClientUtil(BufferedReader reader, BufferedWriter writer) {
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
            StringBuilder meaningStr = new StringBuilder();

            for (String meaning : meanings) {
                meaningStr.append(meaning).append(",");
            }
            meaningStr.replace(meaningStr.length()-1, meaningStr.length(),"");

            writer.write(ADD + COLON + word.toLowerCase() + COLON + meaningStr);
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
        logger.info("update query server: "+ queryServer(word));
        return Mapper.convertStringToArrayList(queryServer(word), CommunicateConfig.END_OF_LINE);
    }
}
