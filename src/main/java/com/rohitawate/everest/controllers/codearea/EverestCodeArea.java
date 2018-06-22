package com.rohitawate.everest.controllers.codearea;

import com.rohitawate.everest.controllers.codearea.highlighters.Highlighter;
import com.rohitawate.everest.controllers.codearea.highlighters.JSONHighlighter;
import com.rohitawate.everest.controllers.codearea.highlighters.XMLHighlighter;
import com.rohitawate.everest.util.settings.Settings;
import javafx.geometry.Insets;
import org.fxmisc.richtext.CodeArea;

import java.time.Duration;

public class EverestCodeArea extends CodeArea {
    public enum HighlightMode {
        JSON, XML, HTML, PLAIN
    }

    private Highlighter highlighter;
    private JSONHighlighter jsonHighlighter;
    private XMLHighlighter xmlHighlighter;

    public EverestCodeArea() {
        this.getStylesheets().add(getClass().getResource("/css/syntax/Moondust.css").toString());
        this.getStyleClass().add("everest-code-area");
        this.setWrapText(Settings.editorWrapText);
        this.setPadding(new Insets(5));

        jsonHighlighter = new JSONHighlighter();
        xmlHighlighter = new XMLHighlighter();

        setMode(HighlightMode.PLAIN);

        this.multiPlainChanges()
                .successionEnds(Duration.ofMillis(1))
                .subscribe(ignore -> this.setStyleSpans(0, highlighter.computeHighlighting(getText())));
    }

    public void setMode(HighlightMode mode) {
        switch (mode) {
            case JSON:
                highlighter = jsonHighlighter;
                break;
            default:
                highlighter = xmlHighlighter;
                break;
        }

        // Re-computes the highlighting for the new mode
        this.setStyleSpans(0, highlighter.computeHighlighting(getText()));
    }

    public void setText(String text, HighlightMode mode) {
        clear();
        appendText(text);
        setMode(mode);
    }
}
