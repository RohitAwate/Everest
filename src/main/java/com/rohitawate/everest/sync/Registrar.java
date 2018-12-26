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

package com.rohitawate.everest.sync;

import com.rohitawate.everest.exceptions.DuplicateException;
import com.rohitawate.everest.preferences.PreferencesManager;
import com.rohitawate.everest.project.ProjectManager;

import static com.rohitawate.everest.sync.SyncManager.prefsManagers;
import static com.rohitawate.everest.sync.SyncManager.projectManagers;

/**
 * Utility class that registers Managers for use by SyncManager.
 */
public class Registrar {
    /**
     * Registers a new ProjectManager to be used for syncing Everest's data
     * at various sources.
     */
    public static void registerManager(ProjectManager manager) throws DuplicateException {
        if (projectManagers.containsKey(manager.getIdentifier()))
            throw new DuplicateException("ProjectManager", manager.getIdentifier());
        else
            projectManagers.put(manager.getIdentifier(), manager);
    }

    /**
     * Registers a new Preferences to be used for syncing the user's preferences
     * at various sources.
     */
    public static void registerManager(PreferencesManager manager) throws DuplicateException {
        if (prefsManagers.containsKey(manager.getIdentifier()))
            throw new DuplicateException("PreferencesManager", manager.getIdentifier());
        else
            prefsManagers.put(manager.getIdentifier(), manager);
    }
}
