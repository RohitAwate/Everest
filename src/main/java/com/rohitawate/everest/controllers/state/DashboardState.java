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

package com.rohitawate.everest.controllers.state;

import com.rohitawate.everest.controllers.DashboardController.ResponseLayer;

import java.util.HashMap;

public class DashboardState {
    public ComposerState composer;
    public ResponseLayer visibleLayer;

    // ResponseLayer parameters
    public int responseCode;
    public String responseType;
    public String responseBody;
    public int responseTime;
    public int responseSize;

    // ErrorLayer parameters
    public String errorTitle;
    public String errorDetails;

    public HashMap<String, String> responseHeaders;

    public DashboardState() {
    }

    public DashboardState(ComposerState composer) {
        this.composer = composer;
    }
}
