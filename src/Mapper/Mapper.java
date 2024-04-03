package Mapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static Config.CommunicateConfig.*;

public class Mapper {
    public static ArrayList<String> convertStringToArrayList(String input, String symbol, Boolean needDecode) {
        // Split the input string by commas
        String[] items = input.split(symbol);

        ArrayList<String> list = new ArrayList<>();
        for (String item : items) {
            if (needDecode) {
                byte[] decodedBytes = Base64.getDecoder().decode(item);
                String decodedMeaning = new String(decodedBytes, StandardCharsets.UTF_8);
                list.add(decodedMeaning);
            } else {
                list.add(item.trim());
            }

        }

        return list;
    }

    public static String convertQueryResultToResultArea (String fromServer) {
        String[] items = fromServer.split(END_OF_LINE);
        if (items.length == 1 && items[0].equals(ERROR_COMMUNICATE)){
            return ERROR_COMMUNICATE;
        }
        StringBuilder queryResult = new StringBuilder();
        int count = 1;
        for (String item : items) {
            queryResult.append(count++)
                    .append(COLON)
                    .append(" ")
                    .append(item)
                    .append(END_OF_LINE);
        }

        return queryResult.toString();

    }

    public static String incomingMeaningToServer(List<String> meanings) {
        StringBuilder meaningStr = new StringBuilder();

        for (String meaning : meanings) {
            String encoded = Base64.getEncoder().encodeToString(meaning.getBytes(StandardCharsets.UTF_8));
            meaningStr.append(encoded).append(",");
        }
        meaningStr.replace(meaningStr.length()-1, meaningStr.length(),"");

        return meaningStr.toString();
    }
}
