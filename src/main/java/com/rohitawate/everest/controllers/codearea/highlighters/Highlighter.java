package com.rohitawate.everest.controllers.codearea.highlighters;

import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

public interface Highlighter {
    StyleSpans<Collection<String>> computeHighlighting(String text);
}
