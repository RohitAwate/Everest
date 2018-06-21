package com.rohitawate.everest.controllers.codearea.highlighters;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONHighlighter implements Highlighter {

    private static final String JSON_CURLY = "(?<JSONCURLY>\\{|\\})";
    private static final String JSON_PROPERTY = "(?<JSONPROPERTY>\\\".*\\\")\\s*:\\s*";
    private static final String JSON_VALUE = "(?<JSONVALUE>\\\".*\\\")";
    private static final String JSON_ARRAY = "\\[(?<JSONARRAY>.*)\\]";
    private static final String JSON_NUMBER = "(?<JSONNUMBER>\\d*.?\\d*)";
    private static final String JSON_BOOL = "(?<JSONBOOL>true|false)";

    private static final Pattern FINAL_REGEX = Pattern.compile(
            JSON_CURLY + "|"
                    + JSON_PROPERTY + "|"
                    + JSON_VALUE + "|"
                    + JSON_ARRAY + "|"
                    + JSON_BOOL + "|"
                    + JSON_NUMBER
    );

    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = FINAL_REGEX.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass
                    = matcher.group("JSONPROPERTY") != null ? "json_property"
                    : matcher.group("JSONVALUE") != null ? "json_value"
                    : matcher.group("JSONARRAY") != null ? "json_array"
                    : matcher.group("JSONCURLY") != null ? "json_curly"
                    : matcher.group("JSONBOOL") != null ? "json_bool"
                    : matcher.group("JSONNUMBER") != null ? "json_number"
                    : null;
            /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
