package Mapper;

import java.util.ArrayList;
import java.util.List;

import static Config.CommunicateConfig.COLON;
import static Config.CommunicateConfig.END_OF_LINE;

public class Mapper {
    public static ArrayList<String> convertStringToArrayList(String input, String symbol) {
        // Split the input string by commas
        String[] items = input.split(symbol);

        ArrayList<String> list = new ArrayList<>();
        for (String item : items) {
            list.add(item.trim());
        }

        return list;
    }

    public static String convertQueryResultToResultArea (String fromServer) {
        String[] items = fromServer.split(END_OF_LINE);
        StringBuilder queryResult = new StringBuilder();
        int count = 1;
        for (String item : items) {
            queryResult.append("Definition ")
                    .append(count++)
                    .append(COLON)
                    .append(item)
                    .append(END_OF_LINE);
        }

        return queryResult.toString();

    }

    public static String incomingMeaningToServer(List<String> meanings) {
        StringBuilder meaningStr = new StringBuilder();

        for (String meaning : meanings) {
            meaningStr.append(meaning).append(",");
        }
        meaningStr.replace(meaningStr.length()-1, meaningStr.length(),"");

        return meaningStr.toString();
    }
}
