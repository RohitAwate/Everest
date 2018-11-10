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

package com.rohitawate.everest.sync.saver;

import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.preferences.Preferences;
import com.rohitawate.everest.preferences.PreferencesManager;

import java.time.LocalDateTime;
import java.util.Collection;

public class PreferencesSaver implements ResourceSaver {
    private Preferences prefs;
    private Collection<PreferencesManager> prefsManagers;

    public PreferencesSaver(Collection<PreferencesManager> prefsManager) {
        this.prefsManagers = prefsManager;
    }

    @Override
    public void setResource(Object resource) {
        this.prefs = (Preferences) resource;
    }

    @Override
    public void run() {
        try {
            for (PreferencesManager manager : prefsManagers)
                manager.savePrefs(prefs);
        } catch (Exception e) {
            LoggingService.logSevere("Could not save history.", e, LocalDateTime.now());
        }
    }
}
