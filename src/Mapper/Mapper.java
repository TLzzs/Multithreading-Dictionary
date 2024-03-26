package Mapper;

import java.util.ArrayList;

import static Util.CommunicateConfig.COLON;
import static Util.CommunicateConfig.END_OF_LINE;

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
        String[] items = fromServer.split("\n");
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
}
