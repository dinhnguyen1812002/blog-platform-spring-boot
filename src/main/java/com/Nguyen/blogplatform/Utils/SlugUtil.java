package com.Nguyen.blogplatform.Utils;

import java.util.regex.Pattern;

public class SlugUtil {
    // Cho phép Unicode letters (\p{L}), digits (\p{N}), dấu gạch ngang và gạch dưới
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\p{L}\\p{N}-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public static String createSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String slug = input.toLowerCase();

        // Thay khoảng trắng bằng -
        slug = WHITESPACE.matcher(slug).replaceAll("-");

        // Loại bỏ ký tự không hợp lệ
        slug = NON_ALPHANUMERIC.matcher(slug).replaceAll("-");

        // Loại bỏ nhiều dấu - liên tiếp
        slug = slug.replaceAll("-+", "-");

        // Bỏ dấu - ở đầu/cuối (nếu có)
        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }
}
