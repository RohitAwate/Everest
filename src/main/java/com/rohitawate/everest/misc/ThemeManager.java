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
import com.rohitawate.everest.logging.Logger;
import javafx.scene.Parent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    public static final String DEFAULT_THEME = "Adreana";
    public static final String DEFAULT_SYNTAX_THEME = "Moondust";

    private static String theme = Main.preferences.appearance.theme;
    private static String syntaxTheme = Main.preferences.appearance.syntaxTheme;

    private static final List<Parent> parentNodes = new ArrayList<>();
    private static File themeFile = new File("Everest/themes/" + theme + ".css");
    private static File syntaxThemeFile = new File("Everest/themes/syntax/" + syntaxTheme + ".css");

    /**
     * Refreshes the theme of all the registered parents by replacing
     * the old external one with the new one. The fallback theme ie "Adreana"
     * is always retained.
     */
    public static void refreshTheme() {
        if (!theme.equals(DEFAULT_THEME)) {
            if (themeFile.exists()) {
                String themePath = themeFile.toURI().toString();

                for (Parent parent : parentNodes) {
                    parent.getStylesheets().remove(1);
                    parent.getStylesheets().add(1, themePath);
                }

                Logger.info("Theme changed to " + theme + ".");
            } else {
                Logger.info(theme + ": No such theme file found.");
                theme = DEFAULT_THEME;
            }
        }
    }

    public static void setTheme(Parent parent) {
        if (!theme.equals(DEFAULT_THEME)) {
            if (themeFile.exists()) {
                parent.getStylesheets().add(themeFile.toURI().toString());
                parentNodes.add(parent);
            } else {
                Logger.info(theme + ": No such theme file found.");
                theme = DEFAULT_THEME;
            }
        }
    }

    public static void setSyntaxTheme(EverestCodeArea everestCodeArea) {
        if (!syntaxTheme.equals(DEFAULT_SYNTAX_THEME)) {
            if (syntaxThemeFile.exists()) {
                everestCodeArea.getStylesheets().add(syntaxThemeFile.toURI().toString());
            } else {
                Logger.info(syntaxTheme + ": No such theme file found.");
                syntaxTheme = DEFAULT_SYNTAX_THEME;
            }
        }
    }
}
