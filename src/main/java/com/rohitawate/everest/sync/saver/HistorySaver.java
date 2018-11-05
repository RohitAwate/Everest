package com.rohitawate.everest.sync.saver;

import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.project.ProjectManager;
import com.rohitawate.everest.state.ComposerState;

import java.time.LocalDateTime;
import java.util.Collection;

public class HistorySaver implements ResourceSaver {
    private ComposerState state;
    private Collection<ProjectManager> projectManagers;

    public HistorySaver(Collection<ProjectManager> projectManagers) {
        this.projectManagers = projectManagers;
    }

    @Override
    public void setResource(Object o) {
        this.state = (ComposerState) o;
    }

    @Override
    public void run() {
        try {
            for (ProjectManager manager : projectManagers)
                manager.saveState(state);
        } catch (Exception e) {
            LoggingService.logSevere("Could not save history.", e, LocalDateTime.now());
        }
    }
}
