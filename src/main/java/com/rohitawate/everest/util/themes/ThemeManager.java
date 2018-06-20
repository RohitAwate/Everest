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

package com.rohitawate.everest.util.themes;

import com.rohitawate.everest.controllers.responsearea.EverestCodeArea;
import com.rohitawate.everest.util.Services;
import com.rohitawate.everest.util.settings.Settings;
import javafx.scene.Parent;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static List<Parent> parentNodes = new ArrayList<>();
    private static File themeFile = new File("Everest/themes/" + Settings.theme + ".css");
    private static File syntaxThemeFile = new File("Everest/themes/syntax/" + Settings.syntaxTheme + ".css");

    /**
     * Refreshes the theme of all the registered parents by replacing
     * the old external one with the new one. The fallback theme ie "Adreana"
     * is always retained.
     */
    public static void refreshTheme() {
        if (!Settings.theme.equals("Adreana")) {
            if (themeFile.exists()) {
                String themePath = themeFile.toURI().toString();

                for (Parent parent : parentNodes) {
                    parent.getStylesheets().remove(1);
                    parent.getStylesheets().add(1, themePath);
                }

                Services.loggingService.logInfo("Theme changed to " + Settings.theme + ".", LocalDateTime.now());
            } else {
                Services.loggingService.logInfo(Settings.theme + ": No such theme file found.", LocalDateTime.now());
            }
        }
    }

    public static void setTheme(Parent parent) {
        if (!Settings.theme.equals("Adreana")) {
            if (themeFile.exists()) {
                parent.getStylesheets().add(themeFile.toURI().toString());
                parentNodes.add(parent);
            } else {
                Services.loggingService.logInfo(Settings.theme + ": No such theme file found.", LocalDateTime.now());
            }
        }
    }

    public static void setSyntaxTheme(EverestCodeArea everestCodeArea) {
        if (!Settings.theme.equals("Ganges")) {
            if (syntaxThemeFile.exists()) {
                everestCodeArea.getStylesheets().add(syntaxThemeFile.toURI().toString());
            } else {
                Services.loggingService.logInfo(Settings.syntaxTheme + ": No such theme file found.", LocalDateTime.now());
            }
        }
    }
}
