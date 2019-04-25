/*
 * Copyright 2019 Rohit Awate.
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
    public String authMethod;

    public List<FieldState> params;
    public List<FieldState> headers;
    public String contentType;

    // Body and content-type of requests with raw bodies
    public String rawBody;
    public String rawBodyBoxValue;

    public AuthState authState;

    // Tuples of URL-encoded requests
    public List<FieldState> urlStringTuples;

    // String and file tuples of multipart-form requests
    public List<FieldState> formStringTuples;
    public List<FieldState> formFileTuples;

    // File path of application/octet-stream requests
    public String binaryFilePath;

    public ComposerState() {
        this.httpMethod = HTTPConstants.GET;
        this.authState = new AuthState();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComposerState that = (ComposerState) o;
        return toString().equals(that.toString());
    }

    @Override
    public String toString() {
        return "ComposerState{" +
                "target='" + target + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", authMethod='" + authMethod + '\'' +
                ", params=" + params +
                ", headers=" + headers +
                ", contentType='" + contentType + '\'' +
                ", rawBody='" + rawBody + '\'' +
                ", rawBodyBoxValue='" + rawBodyBoxValue + '\'' +
                ", authState=" + authState +
                ", urlStringTuples=" + urlStringTuples +
                ", formStringTuples=" + formStringTuples +
                ", formFileTuples=" + formFileTuples +
                ", binaryFilePath='" + binaryFilePath + '\'' +
                '}';
    }
}
