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

import com.rohitawate.everest.sync.EverestManager;

/**
 * Manages the user's preferences which may be stored in a certain location.
 * For example, a local file or Summit.
 */
public interface PreferencesManager extends EverestManager {
    /**
     * Loads the user's preferences from a certain resource.
     * @return the user's preferences
     */
    Preferences loadPrefs() throws Exception;

    /**
     * Saves the user's preferences back to the resource.
     */
    void savePrefs(Preferences prefs) throws Exception;
}
