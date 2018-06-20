package com.rohitawate.everest.controllers.responsearea;

import com.rohitawate.everest.controllers.responsearea.highlighters.Highlighter;
import com.rohitawate.everest.controllers.responsearea.highlighters.JSONHighlighter;
import org.fxmisc.richtext.CodeArea;

import java.time.Duration;

public class EverestCodeArea extends CodeArea {
    public enum HighlightMode {
        JSON, XML, NONE
    }

    private Highlighter highlighter;
    private JSONHighlighter jsonHighlighter;

    public EverestCodeArea() {
        this.getStylesheets().add(getClass().getResource("/css/syntax/Ganges.css").toString());
        this.getStyleClass().add("everest-code-area");

        this.multiPlainChanges()
                .successionEnds(Duration.ofMillis(1))
                .subscribe(ignore -> this.setStyleSpans(0, highlighter.computeHighlighting(getText())));

        jsonHighlighter = new JSONHighlighter();
    }

    public void setText(String text, HighlightMode mode) {
        clear();
        appendText(text);

        highlighter = mode == HighlightMode.JSON ? jsonHighlighter : jsonHighlighter;
    }
}
