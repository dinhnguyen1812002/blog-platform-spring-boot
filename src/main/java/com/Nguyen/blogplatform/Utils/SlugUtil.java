package com.Nguyen.blogplatform.Utils;

import org.springframework.cglib.proxy.NoOp;

import java.util.regex.Pattern;

public class SlugUtil {
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public static String createSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String slug = input.toLowerCase();
        slug = NON_ALPHANUMERIC.matcher(slug).replaceAll("-");
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        return slug;
    }
}
