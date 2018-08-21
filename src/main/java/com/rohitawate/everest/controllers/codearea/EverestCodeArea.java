/*
 * Copyright 2018 Rohit Awate.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rohitawate.everest.controllers.codearea;

import com.rohitawate.everest.controllers.codearea.highlighters.Highlighter;
import com.rohitawate.everest.format.Formatter;
import com.rohitawate.everest.settings.Settings;
import javafx.geometry.Insets;
import org.fxmisc.richtext.CodeArea;

import java.io.IOException;
import java.time.Duration;

public class EverestCodeArea extends CodeArea {
    private Highlighter highlighter;

    public EverestCodeArea() {
        this.getStylesheets().add(getClass().getResource("/css/syntax/Moondust.css").toString());
        this.getStyleClass().add("everest-code-area");
        this.setWrapText(Settings.editorWrapText);
        this.setPadding(new Insets(5));

        this.multiPlainChanges()
                .successionEnds(Duration.ofMillis(1))
                .subscribe(ignore -> highlight());
    }

    private void highlight() {
        this.setStyleSpans(0, highlighter.computeHighlighting(getText()));
    }

    public void setHighlighter(Highlighter highlighter) {
        this.highlighter = highlighter;

        // Re-computes the highlighting using the new Highlighter
        this.highlight();
    }

    /**
     * Sets the text and then computes the highlighting.
     */
    public void setText(String text, Highlighter highlighter) {
        clear();
        appendText(text);
        setHighlighter(highlighter);
    }

    /**
     * Formats the text with the provided Formatter if it is not null,
     * sets the text and then computes the highlighting.
     *
     */
    public void setText(String text, Formatter formatter, Highlighter highlighter) {
        clear();
        String formattedText = text;

        if (formatter != null) {
            try {
                formattedText = formatter.format(text);
            } catch (IOException e) {
                clear();
                appendText(text);
                return;
            }
        }

        appendText(formattedText);
        setHighlighter(highlighter);
    }
}
