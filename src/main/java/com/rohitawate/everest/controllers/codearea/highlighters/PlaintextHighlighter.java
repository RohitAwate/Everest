package com.rohitawate.everest.controllers.codearea.highlighters;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;

public class PlaintextHighlighter implements Highlighter {
    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();

        spansBuilder.add(Collections.singleton("plain-text"), text.length());
        return spansBuilder.create();
    }
}
