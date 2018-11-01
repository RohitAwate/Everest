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

package com.rohitawate.everest.misc;

import com.rohitawate.everest.Main;
import com.rohitawate.everest.controllers.codearea.EverestCodeArea;
import com.rohitawate.everest.logging.LoggingService;
import javafx.scene.Parent;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    public static final String DEFAULT_THEME = "Adreana";
    public static final String DEFAULT_SYNTAX_THEME = "Moondust";

    private static List<Parent> parentNodes = new ArrayList<>();
    private static File themeFile = new File("Everest/themes/" + Main.preferences.appearance.theme + ".css");
    private static File syntaxThemeFile = new File("Everest/themes/syntax/" + Main.preferences.appearance.syntaxTheme + ".css");

    /**
     * Refreshes the theme of all the registered parents by replacing
     * the old external one with the new one. The fallback theme ie "Adreana"
     * is always retained.
     */
    public static void refreshTheme() {
        if (!Main.preferences.appearance.theme.equals(DEFAULT_THEME)) {
            if (themeFile.exists()) {
                String themePath = themeFile.toURI().toString();

                for (Parent parent : parentNodes) {
                    parent.getStylesheets().remove(1);
                    parent.getStylesheets().add(1, themePath);
                }

                LoggingService.logInfo("Theme changed to " + Main.preferences.appearance.theme + ".", LocalDateTime.now());
            } else {
                LoggingService.logInfo(Main.preferences.appearance.theme + ": No such theme file found.", LocalDateTime.now());
                Main.preferences.appearance.theme = DEFAULT_THEME;
            }
        }
    }

    public static void setTheme(Parent parent) {
        if (!Main.preferences.appearance.theme.equals("Adreana")) {
            if (themeFile.exists()) {
                parent.getStylesheets().add(themeFile.toURI().toString());
                parentNodes.add(parent);
            } else {
                LoggingService.logInfo(Main.preferences.appearance.theme + ": No such theme file found.", LocalDateTime.now());
                Main.preferences.appearance.theme = DEFAULT_THEME;
            }
        }
    }

    public static void setSyntaxTheme(EverestCodeArea everestCodeArea) {
        if (!Main.preferences.appearance.syntaxTheme.equals(DEFAULT_SYNTAX_THEME)) {
            if (syntaxThemeFile.exists()) {
                everestCodeArea.getStylesheets().add(syntaxThemeFile.toURI().toString());
            } else {
                LoggingService.logInfo(Main.preferences.appearance.syntaxTheme + ": No such theme file found.", LocalDateTime.now());
                Main.preferences.appearance.syntaxTheme = DEFAULT_SYNTAX_THEME;
            }
        }
    }
}
