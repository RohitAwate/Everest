package com.rohitawate.everest.controllers.codearea;

import com.rohitawate.everest.controllers.codearea.highlighters.Highlighter;
import com.rohitawate.everest.controllers.codearea.highlighters.JSONHighlighter;
import com.rohitawate.everest.util.settings.Settings;
import org.fxmisc.richtext.CodeArea;

import java.time.Duration;

public class EverestCodeArea extends CodeArea {
    public enum HighlightMode {
        JSON, XML, HTML, NONE
    }

    private Highlighter highlighter;
    private JSONHighlighter jsonHighlighter;

    public EverestCodeArea() {
        this.getStylesheets().add(getClass().getResource("/css/syntax/Moondust.css").toString());
        this.getStyleClass().add("everest-code-area");
        this.setWrapText(Settings.editorWrapText);

        jsonHighlighter = new JSONHighlighter();
        setMode(HighlightMode.NONE);

        this.multiPlainChanges()
                .successionEnds(Duration.ofMillis(1))
                .subscribe(ignore -> this.setStyleSpans(0, highlighter.computeHighlighting(getText())));
    }

    public void setMode(HighlightMode mode) {
        highlighter = mode == HighlightMode.JSON ? jsonHighlighter : jsonHighlighter;
    }

    public void setText(String text, HighlightMode mode) {
        clear();
        appendText(text);
        setMode(mode);
    }
}
