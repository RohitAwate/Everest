package com.rohitawate.everest.sync;

import com.google.common.util.concurrent.MoreExecutors;
import com.rohitawate.everest.controllers.HomeWindowController;
import com.rohitawate.everest.exceptions.DuplicateException;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.settings.Settings;
import com.rohitawate.everest.state.ComposerState;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Manages all the DataManagers of Everest and registers new ones.
 * Registers Everest's default SQLiteManager automatically.
 */
public class SyncManager {
    private static HashMap<String, DataManager> managers;
    private static HomeWindowController homeWindowController;
    private static Executor executor = MoreExecutors.directExecutor();
    private static HistorySaver historySaver;

    /**
     * @param homeWindowController - to add a HistoryItem by invoking addHistoryItem()
     */
    public SyncManager(HomeWindowController homeWindowController) {
        SyncManager.homeWindowController = homeWindowController;
        managers = new HashMap<>();
        historySaver = new HistorySaver();

        // Registering the default
        try {
            registerManager(new SQLiteManager());
        } catch (DuplicateException e) {
            System.out.println("SQLite Manager already exists: Nope, will never happen.");
        }
    }

    /**
     * Asynchronously saves the new state by invoking all the registered DataManagers.
     */
    public void saveState(ComposerState newState) {
        // Compares new state with the last added state from the primary fetch source
        if (newState.equals(managers.get(Settings.fetchSource).getLastAdded()))
            return;

        historySaver.newState = newState;
        executor.execute(historySaver);

        homeWindowController.addHistoryItem(newState);
    }

    /**
     * Retrieves the history from the configured source.
     *
     * @return A list of all the requests
     */
    public List<ComposerState> getHistory() {
        List<ComposerState> history = null;
        try {
            if (managers.get(Settings.fetchSource) == null) {
                LoggingService.logSevere("No such source found: " + Settings.fetchSource, null, LocalDateTime.now());
                history = managers.get("SQLite").getHistory();
            } else {
                history = managers.get(Settings.fetchSource).getHistory();
            }
        } catch (Exception e) {
            LoggingService.logSevere("History could not be fetched.", e, LocalDateTime.now());
        }

        return history;
    }

    /**
     * Registers a new DataManager to be used for syncing Everest's data
     * at various sources.
     */
    public void registerManager(DataManager newManager) throws DuplicateException {
        if (managers.containsKey(newManager.getIdentifier()))
            throw new DuplicateException(
                    "Duplicate DataManager: Manager with identifier \'" + newManager.getIdentifier() + "\' already exists.");
        else
            managers.put(newManager.getIdentifier(), newManager);
    }

    private static class HistorySaver implements Runnable {
        private ComposerState newState;

        @Override
        public void run() {
            try {
                for (DataManager manager : managers.values())
                    manager.saveState(newState);
            } catch (Exception e) {
                LoggingService.logSevere("Could not save history.", e, LocalDateTime.now());
            }
        }
    }
}
