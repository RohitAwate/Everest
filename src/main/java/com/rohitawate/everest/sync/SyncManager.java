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

package com.rohitawate.everest.sync;

import com.rohitawate.everest.Main;
import com.rohitawate.everest.controllers.HomeWindowController;
import com.rohitawate.everest.exceptions.DuplicateException;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.preferences.LocalPreferencesManager;
import com.rohitawate.everest.preferences.Preferences;
import com.rohitawate.everest.preferences.PreferencesManager;
import com.rohitawate.everest.project.ProjectManager;
import com.rohitawate.everest.project.SQLiteManager;
import com.rohitawate.everest.state.ComposerState;
import com.rohitawate.everest.sync.saver.HistorySaver;
import com.rohitawate.everest.sync.saver.PreferencesSaver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Manages all the ProjectManagers and PreferencesManagers of Everest and registers new ones.
 * Also, registers Everest's default implementations of the above viz. SQLiteManager and LocalPreferencesManager.
 */
public class SyncManager {
    private static HomeWindowController homeWindowController;
    private static ExecutorService executor = EverestUtilities.newDaemonSingleThreadExecutor();

    // Selected sources
    private static final File SYNC_PREFS_FILE = new File("Everest/config/sync_preferences.json");
    private static SyncPrefs syncPrefs;

    // Default sources
    public static final String DEFAULT_PROJECT_SOURCE = "SQLite";
    public static final String DEFAULT_PREFS_SOURCE = "Local";

    // Managers
    static HashMap<String, ProjectManager> projectManagers = new HashMap<>();
    static HashMap<String, PreferencesManager> prefsManagers = new HashMap<>();

    // ResourceSavers
    private static HistorySaver historySaver = new HistorySaver(projectManagers.values());
    private static PreferencesSaver prefsSaver = new PreferencesSaver(prefsManagers.values());

    static {
        loadSyncPrefs();

        // Registering the default
        try {
            Registrar.registerManager(new SQLiteManager());
            Registrar.registerManager(new LocalPreferencesManager());
        } catch (DuplicateException e) {
            Logger.severe(e.getMessage(), e);
        }
    }

    /**
     * Asynchronously saves the new state by invoking all the registered DataManagers.
     */
    public static void saveState(ComposerState newState) {
        String projectSource = syncPrefs.projectSource;

        if (projectManagers.get(projectSource) == null) {
            projectSource = DEFAULT_PROJECT_SOURCE;
        }

        // Compares new state with the last added state from the primary fetch source
        if (newState.equals(projectManagers.get(projectSource).getLastAdded()))
            return;

        historySaver.setResource(newState);
        executor.execute(historySaver);

        if (Main.preferences.appearance.showHistoryRange > 0) {
            try {
                homeWindowController.addHistoryItem(newState);
            } catch (NullPointerException e) {
                Logger.warning("HomeWindowController not registered. SyncManager could add history item.", e);
            }
        }
    }

    /**
     * Retrieves the history from the configured source.
     *
     * @return a list of all the requests
     */
    public static List<ComposerState> getHistory() {
        String projectSource = syncPrefs.projectSource;

        List<ComposerState> history = null;
        try {
            if (projectManagers.get(projectSource) == null) {
                Logger.severe("No such source found: " + projectSource, null);
                projectSource = DEFAULT_PROJECT_SOURCE;
            }

            history = projectManagers.get(projectSource).getHistory();
        } catch (Exception e) {
            Logger.severe("History could not be fetched.", e);
        }

        return history;
    }

    /**
     * Retrieves the user's preferences from the configured source.
     *
     * @return the user's preferences
     */
    public static Preferences loadPrefs() {
        String prefsSource = syncPrefs.prefsSource;

        if (prefsManagers.get(prefsSource) == null) {
            Logger.severe("No such source found: " + prefsSource, null);
            prefsSource = DEFAULT_PREFS_SOURCE;
        }

        Preferences prefs;

        try {
            prefs = prefsManagers.get(prefsSource).loadPrefs();
            Logger.info("Preferences loaded.");
        } catch (Exception e) {
            Logger.info("Could not load preferences. Everest will use the default values.");
            prefs = new Preferences();
        }

        return prefs;
    }

    /**
     * Asynchronously saves the user's preferences by invoking all the registered PreferencesManagers.
     */
    public static void savePrefs(Preferences prefs) {
        prefsSaver.setResource(prefs);
        executor.execute(prefsSaver);
    }

    public static void setHomeWindowController(HomeWindowController controller) {
        if (homeWindowController != null) {
            Logger.info("HomeWindowController already registered with SyncManager.");
            return;
        }

        homeWindowController = controller;
    }

    private static void loadSyncPrefs() {
        if (!SYNC_PREFS_FILE.exists()) {
            Logger.info("Sync preferences file not found. Everest will use the default values.");
            syncPrefs = new SyncPrefs();
        } else {
            try {
                syncPrefs = EverestUtilities.jsonMapper.readValue(SYNC_PREFS_FILE, SyncPrefs.class);
            } catch (IOException e) {
                Logger.info("Could not load sync preferences. Everest will use the default values.");
                syncPrefs = new SyncPrefs();
            }
        }

        Logger.info("Sync preferences loaded.");
    }

    public static void saveSyncPrefs() {
        try {
            EverestUtilities.jsonMapper.writeValue(SYNC_PREFS_FILE, syncPrefs);
            Logger.info("Sync preferences saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
