package com.rohitawate.everest.controllers.responsearea.highlighters;

import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

public interface Highlighter {
    StyleSpans<Collection<String>> computeHighlighting(String text);
}
