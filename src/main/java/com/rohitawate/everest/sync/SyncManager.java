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

import com.rohitawate.everest.Main;
import com.rohitawate.everest.controllers.HomeWindowController;
import com.rohitawate.everest.exceptions.DuplicateException;
import com.rohitawate.everest.logging.LoggingService;
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
import java.time.LocalDateTime;
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
            LoggingService.logSevere(e.getMessage(), e, LocalDateTime.now());
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
                LoggingService.logWarning("HomeWindowController not registered. SyncManager could add history item.", e, LocalDateTime.now());
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
                LoggingService.logSevere("No such source found: " + projectSource, null, LocalDateTime.now());
                projectSource = DEFAULT_PROJECT_SOURCE;
            }

            history = projectManagers.get(projectSource).getHistory();
        } catch (Exception e) {
            LoggingService.logSevere("History could not be fetched.", e, LocalDateTime.now());
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
            LoggingService.logSevere("No such source found: " + prefsSource, null, LocalDateTime.now());
            prefsSource = DEFAULT_PREFS_SOURCE;
        }

        Preferences prefs;

        try {
            prefs = prefsManagers.get(prefsSource).loadPrefs();
            LoggingService.logInfo("Preferences loaded.", LocalDateTime.now());
        } catch (Exception e) {
            LoggingService.logInfo("Could not load preferences. Everest will use the default values.", LocalDateTime.now());
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
            LoggingService.logInfo("HomeWindowController already registered with SyncManager.", LocalDateTime.now());
            return;
        }

        homeWindowController = controller;
    }

    private static void loadSyncPrefs() {
        if (!SYNC_PREFS_FILE.exists()) {
            LoggingService.logInfo("Sync preferences file not found. Everest will use the default values.", LocalDateTime.now());
            syncPrefs = new SyncPrefs();
        } else {
            try {
                syncPrefs = EverestUtilities.jsonMapper.readValue(SYNC_PREFS_FILE, SyncPrefs.class);
            } catch (IOException e) {
                LoggingService.logInfo("Could not load sync preferences. Everest will use the default values.", LocalDateTime.now());
                syncPrefs = new SyncPrefs();
            }
        }

        LoggingService.logInfo("Sync preferences loaded.", LocalDateTime.now());
    }

    public static void saveSyncPrefs() {
        try {
            EverestUtilities.jsonMapper.writeValue(SYNC_PREFS_FILE, syncPrefs);
            LoggingService.logInfo("Sync preferences saved.", LocalDateTime.now());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
