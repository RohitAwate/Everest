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

package com.rohitawate.everest.project;

import com.rohitawate.everest.state.ComposerState;
import com.rohitawate.everest.sync.EverestManager;

import java.util.List;

/**
 * Manages the history and (in the future) the projects of Everest.
 */
public interface ProjectManager extends EverestManager {
    String HEADER = "Header";
    String PARAM = "Param";
    String URL_STRING = "URLString";
    String FORM_STRING = "FormString";
    String AUTH_METHOD = "AuthMethod";
    String FILE = "File";
    String ID = "ID";

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
     * If this ProjectManager is the primary fetching source, SyncManager uses
     * calls this method before attempting to save a new state.
     */
    ComposerState getLastAdded();
}
