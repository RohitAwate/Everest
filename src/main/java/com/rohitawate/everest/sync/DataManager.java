package com.rohitawate.everest.sync;

import com.rohitawate.everest.state.ComposerState;

import java.util.List;

/**
 * Manages the history and (in the future) the projects of Everest.
 */
public interface DataManager {
    /**
     * Saves the state of the Composer when the request was made.
     */
    void saveState(ComposerState newState) throws Exception;

    /**
     * Fetches all the states of the Composer when the previous requests were made.
     *
     * @return A list of the states.
     */
    List<ComposerState> getHistory() throws Exception;

    /**
     * Returns the state of the Composer when the last request was made.
     * If this DataManager is the primary fetching source, SyncManager uses
     * calls this method before attempting to save a new state.
     */
    ComposerState getLastAdded();

    /**
     * Returns the identifier for the DataManager. Preferably, use the source as the identifier.
     * For example, a DataManager using Google Drive may identify itself as 'Google Drive'.
     */
    String getIdentifier();
}
