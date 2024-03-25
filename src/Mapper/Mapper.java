package Mapper;

import java.util.ArrayList;

public class Mapper {
    public static ArrayList<String> convertStringToArrayList(String input) {
        // Split the input string by commas
        String[] items = input.split(",");

        // Create a new ArrayList and add the split items, trimming any spaces
        ArrayList<String> list = new ArrayList<>();
        for (String item : items) {
            list.add(item.trim());
        }

        return list;
    }
}
