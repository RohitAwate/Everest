package com.rohitawate.everest.sync;

public interface EverestManager {
    /**
     * Returns the identifier for the Manager. Preferably, use the source as the identifier.
     * This identifier may be used in the UI, hence, do not add the type of Manager as a suffix.
     *
     * For example, a manager syncing preferences from Summit would be called 'Summit', not 'Summit Preferences Manager'.
     */
    String getIdentifier();
}
