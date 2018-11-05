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
