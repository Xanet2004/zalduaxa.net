package net.zalduaxa.backend.common.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtils {

    private SlugUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String slugify(String input) {
        if (input == null) {
            return "untitled";
        }

        String text = input.trim().toLowerCase(Locale.ROOT);

        text = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        text = text.replaceAll("[^a-z0-9_-]+", "-");
        text = text.replaceAll("^-+|-+$", "");

        return text.isEmpty() ? "untitled" : text;
    }
}