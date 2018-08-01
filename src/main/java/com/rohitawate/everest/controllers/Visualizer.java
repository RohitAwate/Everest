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

import com.fasterxml.jackson.databind.JsonNode;
import com.rohitawate.everest.misc.EverestUtilities;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class Visualizer extends ScrollPane {
    private TreeView<HBox> visualizer;

    Visualizer() {
        visualizer = new TreeView<>();
        visualizer.setShowRoot(false);
        setContent(this.visualizer);

        setFitToHeight(true);
        setFitToWidth(true);
    }

    void populate(String body) throws IOException {
        JsonNode tree = EverestUtilities.jsonMapper.readTree(body);
        this.populate(new TreeItem<>(), "root", tree);
    }

    private void populate(TreeItem<HBox> rootItem, String rootName, JsonNode root) {
        if (rootName.equals("root")) {
            visualizer.setRoot(rootItem);
        }

        Label rootLabel = new Label(rootName);
        rootLabel.getStyleClass().addAll("visualizerRootLabel", "visualizerLabel");
        rootItem.setValue(new HBox(rootLabel));

        JsonNode currentNode;
        Label valueLabel;
        HBox valueContainer;
        List<TreeItem<HBox>> items = new LinkedList<>();
        Tooltip valueTooltip;

        if (root.isArray()) {
            Iterator<JsonNode> iterator = root.elements();
            int i = 0;

            while (iterator.hasNext()) {
                currentNode = iterator.next();

                if (currentNode.isValueNode()) {
                    valueLabel = new Label(i++ + ": " + currentNode.toString());
                    valueLabel.getStyleClass().addAll("visualizerValueLabel", "visualizerLabel");
                    valueLabel.setWrapText(true);
                    valueTooltip = new Tooltip(currentNode.toString());
                    valueLabel.setTooltip(valueTooltip);

                    valueContainer = new HBox(valueLabel);
                    items.add(new TreeItem<>(valueContainer));
                } else if (currentNode.isObject()) {
                    TreeItem<HBox> newRoot = new TreeItem<>();
                    items.add(newRoot);
                    populate(newRoot, i++ + ": [Anonymous Object]", currentNode);
                }
            }
        } else {
            Iterator<Map.Entry<String, JsonNode>> iterator = root.fields();
            Map.Entry<String, JsonNode> currentEntry;
            Label keyLabel;
            Tooltip keyTooltip;

            while (iterator.hasNext()) {
                currentEntry = iterator.next();
                currentNode = currentEntry.getValue();

                if (currentNode.isValueNode()) {
                    keyLabel = new Label(currentEntry.getKey() + ": ");
                    keyLabel.getStyleClass().addAll("visualizerKeyLabel", "visualizerLabel");
                    keyTooltip = new Tooltip(currentEntry.getKey());
                    keyLabel.setTooltip(keyTooltip);

                    valueLabel = new Label(currentNode.toString());
                    valueLabel.getStyleClass().addAll("visualizerValueLabel", "visualizerLabel");
                    valueLabel.setWrapText(true);
                    valueTooltip = new Tooltip(currentNode.toString());
                    valueLabel.setTooltip(valueTooltip);

                    valueContainer = new HBox(keyLabel, valueLabel);
                    items.add(new TreeItem<>(valueContainer));
                } else if (currentNode.isArray() || currentNode.isObject()) {
                    TreeItem<HBox> newRoot = new TreeItem<>();
                    items.add(newRoot);
                    populate(newRoot, currentEntry.getKey(), currentNode);
                }
            }
        }

        rootItem.getChildren().addAll(items);
    }

    public void clear() {
        visualizer.setRoot(null);
    }
}
