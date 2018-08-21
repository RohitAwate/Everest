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

import com.rohitawate.everest.models.requests.HTTPConstants;

import java.util.List;

/**
 * Represents the state of the Composer.
 */
public class ComposerState {
    public String target;
    public String httpMethod;

    public List<FieldState> params;
    public List<FieldState> headers;
    public String contentType;

    // Body and content-type of requests with raw bodies
    public String rawBody;
    public String rawBodyBoxValue;

    // Tuples of URL-encoded requests
    public List<FieldState> urlStringTuples;

    // String and file tuples of multipart-form requests
    public List<FieldState> formStringTuples;
    public List<FieldState> formFileTuples;

    // File path of application/octet-stream requests
    public String binaryFilePath;

    public ComposerState() {
        this.httpMethod = HTTPConstants.GET;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComposerState state = (ComposerState) o;
        if (!target.equals(state.target)) return false;
        if (!httpMethod.equals(state.httpMethod)) return false;
        if (!params.equals(state.params)) return false;
        if (!headers.equals(state.headers)) return false;

        if (state.httpMethod.equals(HTTPConstants.GET)
                || state.httpMethod.equals(HTTPConstants.DELETE)) return true;

        if (!contentType.equals(state.contentType)) return false;
        if (!rawBody.equals(state.rawBody)) return false;
        if (!rawBodyBoxValue.equals(state.rawBodyBoxValue)) return false;
        if (!binaryFilePath.equals(state.binaryFilePath)) return false;
        if (!urlStringTuples.equals(state.urlStringTuples)) return false;
        if (!formStringTuples.equals(state.formStringTuples)) return false;
        if (!formFileTuples.equals(state.formFileTuples)) return false;

        return true;
    }
}
