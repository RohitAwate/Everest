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
