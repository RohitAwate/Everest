package com.rohitawate.everest.controllers.codearea.highlighters;

import com.rohitawate.everest.exceptions.DuplicateHighlighterException;

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
        highlighters.put("JSON", new JSONHighlighter());

        XMLHighlighter xmlHighlighter = new XMLHighlighter();
        highlighters.put("XML", xmlHighlighter);
        highlighters.put("HTML", xmlHighlighter);

        highlighters.put("PLAIN TEXT", new PlaintextHighlighter());
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
     * @throws DuplicateHighlighterException If a Highlighter is already loaded by Everest with the same name.
     */
    public static void addHighlighter(String name, Highlighter highlighter)
            throws DuplicateHighlighterException {
        if (highlighters.containsKey(name)) {
            throw new DuplicateHighlighterException("Highlighter already exists for the following type: " + name);
        }

        highlighters.put(name, highlighter);
    }
}
