package com.rohitawate.everest.format;

import java.io.IOException;

/**
 * Formats strings in various data formats.
 */
public interface Formatter {
    /**
     * Returns a string formatted appropriate to the data format.
     *
     * @param unformatted The unformatted string
     * @throws IOException If the Formatter fails to format the given string.
     */
    String format(String unformatted) throws IOException;
}