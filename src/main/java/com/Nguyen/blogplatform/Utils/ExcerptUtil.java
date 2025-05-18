package com.Nguyen.blogplatform.Utils;

public class ExcerptUtil {

    public static String excerpt(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Tách đoạn văn thành các câu dựa trên dấu chấm, chấm than, chấm hỏi và khoảng trắng theo sau
        String[] sentences = text.split("(?<=[.!?])\\s+");

        // Lấy 2 câu đầu tiên nếu có
        StringBuilder result = new StringBuilder();
        int count = Math.min(2, sentences.length);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(sentences[i]);
        }

        // Nếu độ dài vượt quá 100 ký tự thì cắt ngắn lại
        if (result.length() > 100) {
            return result.substring(0, 100);
        } else {
            return result.toString();
        }
    }

}
