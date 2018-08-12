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

package com.rohitawate.everest.controllers.visualizers;

import com.fasterxml.jackson.databind.JsonNode;
import com.rohitawate.everest.misc.EverestUtilities;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TreeVisualizer extends Visualizer {
    private TreeView<String> visualizer;

    public TreeVisualizer() {
        visualizer = new TreeView<>();
        visualizer.setShowRoot(false);
        visualizer.setCache(true);
        setContent(visualizer);
    }

    public void populate(String body) throws IOException {
        JsonNode tree = EverestUtilities.jsonMapper.readTree(body);
        this.populate(new TreeItem<>(), "root", tree);
    }

    private void populate(TreeItem<String> rootItem, String rootName, JsonNode root) {
        if (rootName.equals("root")) {
            visualizer.setRoot(rootItem);
        }

        rootItem.setValue(rootName);

        JsonNode currentNode;
        List<TreeItem<String>> items = new ArrayList<>();

        if (root.isArray()) {
            Iterator<JsonNode> iterator = root.elements();
            int i = 0;

            while (iterator.hasNext()) {
                currentNode = iterator.next();

                if (currentNode.isValueNode()) {
                    items.add(new TreeItem<>(i++ + ": " + EverestUtilities.trimString(currentNode.toString())));
                } else if (currentNode.isObject()) {
                    TreeItem<String> newRoot = new TreeItem<>();
                    newRoot.setExpanded(true);
                    items.add(newRoot);
                    populate(newRoot, i++ + ": [Anonymous Object]", currentNode);
                }
            }
        } else {
            Iterator<Map.Entry<String, JsonNode>> iterator = root.fields();
            Map.Entry<String, JsonNode> currentEntry;

            while (iterator.hasNext()) {
                currentEntry = iterator.next();
                currentNode = currentEntry.getValue();

                if (currentNode.isValueNode()) {
                    items.add(new TreeItem<>(currentEntry.getKey() + ": "
                            + EverestUtilities.trimString(currentNode.toString())));
                } else if (currentNode.isArray() || currentNode.isObject()) {
                    TreeItem<String> newRoot = new TreeItem<>();
                    newRoot.setExpanded(true);
                    items.add(newRoot);
                    populate(newRoot, currentEntry.getKey(), currentNode);
                }
            }
        }

        rootItem.getChildren().addAll(items);
    }

    public void clear() {
        visualizer.setRoot(null);
        System.gc();
    }
}
