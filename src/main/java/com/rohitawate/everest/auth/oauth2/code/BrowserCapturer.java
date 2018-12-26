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

package com.rohitawate.everest.auth.oauth2.code;

import com.rohitawate.everest.server.CaptureServer;

/**
 * Opens the OAuth 2.0 authorization window in the user's browser
 * and captures the authorization grant by forcing redirects to a
 * local server.
 */
public class BrowserCapturer implements AuthorizationGrantCapturer {
    private String authURL;

    static final String LOCAL_SERVER_URL = "http://localhost:52849/granted";

    BrowserCapturer(String authURL) {
        this.authURL = authURL;
    }

    @Override
    public String getAuthorizationGrant() throws Exception {
        return CaptureServer.capture(authURL);
    }
}
