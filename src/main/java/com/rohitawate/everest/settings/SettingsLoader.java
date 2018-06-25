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

package com.rohitawate.everest.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.misc.Services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Loads up custom values into Settings from Everest/config/settings.json.
 */
public class SettingsLoader implements Runnable {
    public Thread settingsLoaderThread;
    private JsonNode nodes;

    public SettingsLoader() {
        settingsLoaderThread = new Thread(this, "Settings loader thread");
        settingsLoaderThread.start();
    }

    @Override
    public void run() {
        try {
            File settingsFile = new File("Everest/config/settings.json");

            System.out.println("Settings file found. Loading settings... ");

            nodes = EverestUtilities.jsonMapper.readTree(settingsFile);

            Settings.connectionTimeOutEnable = setBooleanSetting(Settings.connectionTimeOutEnable, "connectionTimeOutEnable");
            if (Settings.connectionTimeOutEnable)
                Settings.connectionTimeOut = setIntegerSetting(Settings.connectionTimeOut, "connectionTimeOut");

            Settings.connectionReadTimeOutEnable = setBooleanSetting(Settings.connectionReadTimeOutEnable, "connectionReadTimeOutEnable");
            if (Settings.connectionReadTimeOutEnable)
                Settings.connectionReadTimeOut = setIntegerSetting(Settings.connectionReadTimeOut, "connectionReadTimeOut");

            Settings.editorWrapText = setBooleanSetting(Settings.editorWrapText, "editorWrapText");

            Settings.theme = EverestUtilities.trimString(setStringSetting(Settings.theme, "theme"));
            Settings.syntaxTheme = EverestUtilities.trimString(setStringSetting(Settings.syntaxTheme, "syntaxTheme"));
            Settings.showHistoryRange = setIntegerSetting(Settings.showHistoryRange, "showHistoryRange");
        } catch (IOException IOE) {
            Services.loggingService.logInfo("Settings file not found. Using defaults.", LocalDateTime.now());
        }
    }

    private String setStringSetting(String defaultValue, String identifier) {
        JsonNode value = nodes.get(identifier);

        if (value != null) {
            defaultValue = value.toString();
            Services.loggingService.logInfo("[" + identifier + "]: Loaded: " + defaultValue, LocalDateTime.now());
        } else {
            Services.loggingService.logInfo("[" + identifier + "]: Not found. Using default value.", LocalDateTime.now());
        }

        return defaultValue;
    }

    private int setIntegerSetting(int defaultValue, String identifier) {
        JsonNode value = nodes.get(identifier);

        if (value != null) {
            defaultValue = value.asInt();
            Services.loggingService.logInfo("[" + identifier + "]: Loaded: " + defaultValue, LocalDateTime.now());
        } else {
            Services.loggingService.logInfo("[" + identifier + "]: Not found. Using default value.", LocalDateTime.now());
        }

        return defaultValue;
    }

    private boolean setBooleanSetting(boolean defaultValue, String identifier) {
        JsonNode value = nodes.get(identifier);

        if (value != null) {
            defaultValue = value.asBoolean();
            Services.loggingService.logInfo("[" + identifier + "]: Loaded: " + defaultValue, LocalDateTime.now());
        } else {
            Services.loggingService.logInfo("[" + identifier + "]: Not found. Using default value.", LocalDateTime.now());
        }

        return defaultValue;
    }
}
