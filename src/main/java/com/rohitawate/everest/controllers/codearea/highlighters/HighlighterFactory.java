package com.rohitawate.everest.controllers.codearea.highlighters;

import com.rohitawate.everest.exceptions.DuplicateException;
import com.rohitawate.everest.models.requests.HTTPConstants;

import java.util.HashMap;

/**
 * Provides Highlighters for computing syntax highlighting
 * of specific data formats.
 * <p>
 * Everest, by default, comes with Highlighters for JSON, XML (HTML uses the same) and PLAIN TEXT.
 */
public class HighlighterFactory {
    private static HashMap<String, Highlighter> highlighters;

    static {
        highlighters = new HashMap<>();
        highlighters.put(HTTPConstants.JSON, new JSONHighlighter());

        XMLHighlighter xmlHighlighter = new XMLHighlighter();
        highlighters.put(HTTPConstants.XML, xmlHighlighter);
        highlighters.put(HTTPConstants.HTML, xmlHighlighter);

        highlighters.put(HTTPConstants.PLAIN_TEXT, new PlaintextHighlighter());
    }

    public static Highlighter getHighlighter(String name) {
        return highlighters.get(name);
    }

    /**
     * Provisional method for the future Extension API which will enable
     * developers to write and load custom Highlighters.
     *
     * @param name        The display name for the Highlighter.
     * @param highlighter The Highlighter object
     * @throws DuplicateException If a Highlighter is already loaded by Everest with the same name.
     */
    public static void addHighlighter(String name, Highlighter highlighter)
            throws DuplicateException {
        if (highlighters.containsKey(name)) {
            throw new DuplicateException("Highlighter already exists for the following type: " + name);
        }

        highlighters.put(name, highlighter);
    }
}
