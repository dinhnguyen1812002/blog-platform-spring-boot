package com.Nguyen.blogplatform.shared.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtil {
    /** Leaves room for a uniqueness suffix inside the 255-char column. */
    public static final int MAX_LENGTH = 200;

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\u00A0_\\u2013\\u2014/]+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9-]");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");

    private SlugUtil() {}

    /**
     * Converts text to a URL slug. Handles Vietnamese: "Đường đi Đà Nẵng" → "duong-di-da-nang".
     * Returns an empty string when nothing usable remains (e.g. only emoji) — callers that
     * need a slug must apply a fallback.
     */
    public static String toSlug(String input) {
        if (input == null) return null;

        // Đ/đ are base letters (not d + combining mark), so NFD alone would drop them
        String text = input.trim().replace('Đ', 'D').replace('đ', 'd');
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = COMBINING_MARKS.matcher(text).replaceAll("");
        text = text.toLowerCase(Locale.ROOT);
        text = SEPARATORS.matcher(text).replaceAll("-");
        text = NON_ALNUM.matcher(text).replaceAll("");
        text = MULTI_DASH.matcher(text).replaceAll("-");
        text = trimDashes(text);

        if (text.length() > MAX_LENGTH) {
            text = text.substring(0, MAX_LENGTH);
            int lastDash = text.lastIndexOf('-');
            if (lastDash > MAX_LENGTH / 2) text = text.substring(0, lastDash);
            text = trimDashes(text);
        }
        return text;
    }

    private static String trimDashes(String s) {
        return s.replaceAll("^-+|-+$", "");
    }
}
