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

package com.rohitawate.everest.state;

import com.rohitawate.everest.controllers.BodyTabController.BodyTab;

import java.util.ArrayList;

/**
 * Convenience class to abstract the state of the application.
 */
public class ComposerState {
    public BodyTab visibleBodyTab;

    public String target;

    public String httpMethod;
    public ArrayList<FieldState> params;
    public ArrayList<FieldState> headers;

    // Body and content-type of requests with raw bodies
    public String rawBody;
    public String rawBodyContentType;

    // Tuples of URL-encoded requests
    public ArrayList<FieldState> urlStringTuples;

    // String and file tuples of multipart-form requests
    public ArrayList<FieldState> formStringTuples;
    public ArrayList<FieldState> formFileTuples;

    // File path of application/octet-stream requests
    public String binaryFilePath;

    public ComposerState() {
        this.httpMethod = "GET";
    }
}
