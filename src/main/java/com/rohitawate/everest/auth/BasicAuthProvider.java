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

package com.rohitawate.everest.auth;

import java.util.Base64;
import java.util.Base64.Encoder;

public class BasicAuthProvider implements AuthProvider {
    private static Encoder encoder = Base64.getEncoder();

    private String username;
    private String password;
    private boolean enabled;

    public BasicAuthProvider(String username, String password, boolean enabled) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    @Override
    public String getAuthHeader() {
        return "Basic " + encoder.encodeToString((username + ":" + password).getBytes());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
