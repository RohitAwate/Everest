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

package com.rohitawate.everest.auth.oauth2;

import com.rohitawate.everest.auth.AuthProvider;
import com.rohitawate.everest.auth.oauth2.tokens.OAuth2Token;
import com.rohitawate.everest.state.auth.OAuth2FlowState;

/**
 * Adds OAuth 2.0-specific functionality to AuthProvider.
 */
public interface OAuth2Provider extends AuthProvider {
    /**
     * Returns the access token for the respective API.
     */
    OAuth2Token getAccessToken() throws Exception;

    /**
     * Accepts the state of the corresponding controller
     * to work with.
     */
    void setState(OAuth2FlowState state);
}
