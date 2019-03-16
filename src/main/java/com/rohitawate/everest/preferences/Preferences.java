/*
 * Copyright 2019 Rohit Awate.
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

/**
 * Holds default preferences values which may
 * get overwritten by PreferencesManager.
 */
public class Preferences {
    public RequestPrefs request;
    public AppearancePrefs appearance;
    public EditorPrefs editor;
    public AuthPrefs auth;

    public Preferences() {
        request = new RequestPrefs();
        appearance = new AppearancePrefs();
        editor = new EditorPrefs();
        auth = new AuthPrefs();
    }
}
