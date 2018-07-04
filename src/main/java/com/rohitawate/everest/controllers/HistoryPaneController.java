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
import com.rohitawate.everest.controllers.state.DashboardState;
import com.rohitawate.everest.misc.Services;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class HistoryPaneController extends SearchablePaneController<DashboardState> {

	private List<Consumer<DashboardState>> stateClickHandler = new LinkedList<>();

	@Override
	protected List<DashboardState> loadInitialEntries() {
		return Services.historyManager.getHistory();
	}

	protected SearchEntry<DashboardState> createEntryFromState(DashboardState state) throws IOException {
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

	private void handleClick(DashboardState state) {
		for (Consumer<DashboardState> consumer : stateClickHandler) {
			consumer.accept(state);
		}
	}

	public void addItemClickHandler(Consumer<DashboardState> handler) {
		stateClickHandler.add(handler);
	}
}
