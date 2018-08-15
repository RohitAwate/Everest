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
    void saveState(ComposerState newState);

    /**
     * Fetches all the states of the Composer when the previous requests were made.
     *
     * @return A list of the states.
     */
    List<ComposerState> getHistory();

    /**
     * Returns the identifier for the DataManager. Preferably, use the source as the identifier.
     * For example, a DataManager using Google Drive may identify itself as 'Google Drive'.
     */
    String getIdentifier();
}
