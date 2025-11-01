package com.Nguyen.blogplatform.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtil {
    private static final Pattern NONLATIN = Pattern.compile("[^\u0000-\u007F]");
    private static final Pattern WHITESPACE = Pattern.compile("[\u0020\u00A0]+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9-]");

    private SlugUtil() {}

    public static String toSlug(String input) {
        if (input == null) return null;
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = slug.replaceAll("[\u2013\u2014]", "-");
        slug = NON_ALNUM.matcher(slug).replaceAll("");
        slug = slug.replaceAll("-+", "-");
        return slug.replaceAll("^-|-$", "");
    }
}
