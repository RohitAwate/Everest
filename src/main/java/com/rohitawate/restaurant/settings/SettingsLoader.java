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

package com.rohitawate.restaurant.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SettingsLoader implements Runnable {
    public Thread SettingsLoaderThread;

    public SettingsLoader() {
        SettingsLoaderThread = new Thread(this, "Settings loader thread");
        SettingsLoaderThread.start();
    }

    @Override
    public void run() {
        try {
            StringBuilder settingsJSON = new StringBuilder();
            File settingsFile = new File("settings/settings.json");
            BufferedReader reader = new BufferedReader(new FileReader(settingsFile));

            String line;
            while ((line = reader.readLine()) != null) {
                settingsJSON.append(line).append('\n');
            }

            reader.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode nodes = mapper.readTree(settingsJSON.toString());
            Settings.responseAreaFont = nodes.get("responseAreaFont").toString();
            Settings.responseAreaFontSize = nodes.get("responseAreaFontSize").asInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
