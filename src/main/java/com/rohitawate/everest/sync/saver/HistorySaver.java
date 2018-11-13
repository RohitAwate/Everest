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

package com.rohitawate.everest.sync.saver;

import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.project.ProjectManager;
import com.rohitawate.everest.state.ComposerState;

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
            Logger.severe("Could not save history.", e);
        }
    }
}
