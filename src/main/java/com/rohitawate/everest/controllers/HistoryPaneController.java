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

package com.rohitawate.everest.controllers;

import com.rohitawate.everest.controllers.search.SearchablePaneController;
import com.rohitawate.everest.state.ComposerState;
import com.rohitawate.everest.sync.SyncManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class HistoryPaneController extends SearchablePaneController<ComposerState> {
    private List<Consumer<ComposerState>> stateClickHandler = new LinkedList<>();
	private SyncManager syncManager;

	@Override
    protected List<ComposerState> loadInitialEntries() {
		return syncManager.getHistory();
	}

    protected SearchEntry<ComposerState> createEntryFromState(ComposerState state) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/HistoryItem.fxml"));
		Parent historyItem = loader.load();

		HistoryItemController controller = loader.getController();
		controller.setState(state);

		// Clicking on HistoryItem opens it up in a new tab
		historyItem.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton() == MouseButton.PRIMARY)
				handleClick(state);
		});

		return new SearchEntry<>(historyItem, controller);
	}

    private void handleClick(ComposerState state) {
        for (Consumer<ComposerState> consumer : stateClickHandler) {
			consumer.accept(state);
		}
	}

    public void addItemClickHandler(Consumer<ComposerState> handler) {
		stateClickHandler.add(handler);
	}

	public void setSyncManager(SyncManager syncManager) {
		this.syncManager = syncManager;
	}
}
