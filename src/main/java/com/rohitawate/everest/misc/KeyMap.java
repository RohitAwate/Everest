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

package com.rohitawate.everest.misc;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class KeyMap {
    public final static KeyCombination newTab = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
    public final static KeyCombination closeTab = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
    public final static KeyCombination toggleHistory = new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN);
    public final static KeyCombination focusAddressBar = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
    public final static KeyCombination focusMethodBox = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN);
    public final static KeyCombination sendRequest = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
    public final static KeyCombination searchHistory = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
    public final static KeyCombination focusParams = new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN);
    public final static KeyCombination focusAuth = new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN);
    public final static KeyCombination focusHeaders = new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN);
    public final static KeyCombination focusBody = new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN);
    public final static KeyCombination refreshTheme = new KeyCodeCombination(KeyCode.T, KeyCombination.SHIFT_DOWN);
}
