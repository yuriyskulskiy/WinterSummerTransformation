package com.example.animated.article.custom;

import java.util.LinkedList;
import java.util.List;

public class RegularData {

    private final static String MOCK_TEXT = ". Lorem ipsum dolor sit amet, consectetur adipiscing" +
            " elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut" +
            " aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
            "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint " +
            "occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim " +
            "id est laborum.";

    private final String text;

    public RegularData(String dataText) {
        text = dataText;
    }

    public static List<RegularData> createMockDataSet() {
        List<RegularData> dataList = new LinkedList<>();
        for (int i = 0; i < 50; i++) {
            String message = i + MOCK_TEXT;
            dataList.add(new RegularData(message));
        }
        return dataList;
    }

    public String getText() {
        return text;
    }
}
