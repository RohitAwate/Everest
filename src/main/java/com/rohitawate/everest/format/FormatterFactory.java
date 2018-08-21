package com.rohitawate.everest.format;

import com.rohitawate.everest.exceptions.DuplicateException;
import com.rohitawate.everest.models.requests.HTTPConstants;

import java.util.HashMap;

/**
 * Provides Formatters for formatting strings of specific data formats.
 * <p>
 * Everest, by default, comes with a Formatter for JSON.
 */
public class FormatterFactory {
    private static HashMap<String, Formatter> formatters;

    static {
        formatters = new HashMap<>();
        formatters.put(HTTPConstants.JSON, new JSONFormatter());
    }

    public static Formatter getHighlighter(String type) {
        return formatters.get(type);
    }


    /**
     * Provisional method for the future Extension API which will enable
     * developers to write and load custom Formatters.
     *
     * @param name      The display name for the Formatter.
     * @param formatter The Formatter object.
     * @throws DuplicateException If a Formatter is already loaded by Everest with the same name.
     */
    public static void addFormatter(String name, Formatter formatter)
            throws DuplicateException {
        if (formatters.containsKey(name)) {
            throw new DuplicateException("Formatter already exists for the following type: " + name);
        }

        formatters.put(name, formatter);
    }
}
