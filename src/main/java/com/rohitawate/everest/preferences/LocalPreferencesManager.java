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

package com.rohitawate.everest.preferences;

import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.misc.EverestUtilities;

import java.io.File;
import java.io.IOException;

/**
 * Loads up custom values into Preferences from a local JSON file.
 */
public class LocalPreferencesManager implements PreferencesManager {
    private static final File PREFS_FILE = new File("Everest/config/preferences.json");

    @Override
    public Preferences loadPrefs() throws IOException {
        if (!PREFS_FILE.exists()) {
            Logger.info("Preferences file not found. Everest will use the default values.");
            return new Preferences();
        }

        return EverestUtilities.jsonMapper.readValue(PREFS_FILE, Preferences.class);
    }

    public void savePrefs(Preferences prefs) {
        try {
            EverestUtilities.jsonMapper.writeValue(PREFS_FILE, prefs);
            Logger.info("Application preferences saved to local file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getIdentifier() {
        return "Local";
    }
}
