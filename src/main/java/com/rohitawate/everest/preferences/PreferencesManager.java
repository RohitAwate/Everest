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
