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
import com.rohitawate.everest.controllers.auth.oauth2.OAuth2FlowController;
import com.rohitawate.everest.state.auth.OAuth2FlowState;

import java.util.ArrayList;

/**
 * Adds OAuth 2.0-specific functionality to AuthProvider.
 */
public abstract class OAuth2FlowProvider implements AuthProvider {
    private OAuth2FlowController controller;

    OAuth2FlowProvider(OAuth2FlowController controller) {
        this.controller = controller;
    }

    /**
     * Returns the access token for the respective API.
     */
    abstract OAuth2Token getAccessToken() throws Exception;

    /**
     * Accepts the state of the corresponding controller
     * to work with.
     */
    abstract void setState(OAuth2FlowState state);

    protected boolean running;

    public boolean isRunning() {
        return this.running;
    }

    // Heterogeneous POOL of OAuth 2.0 Provider implementations
    private final static ArrayList<OAuth2FlowProvider> POOL = new ArrayList<>();

    public static OAuth2FlowProvider getProvider(Flow flow, OAuth2FlowController controller) {
        OAuth2FlowProvider provider = null;

        for (var poolProvider : POOL) {
            if (poolProvider.isRunning()) continue;

            switch (flow) {
                case AUTH_CODE:
                    if (poolProvider.getClass().equals(AuthorizationCodeFlowProvider.class)) {
                        provider = poolProvider;
                        break;
                    }
                    break;
                case IMPLICIT:
                    if (poolProvider.getClass().equals(ImplicitFlowProvider.class)) {
                        provider = poolProvider;
                        break;
                    }
                    break;
                case RESOURCE_OWNER_PASSWORD_CREDS:
                    if (poolProvider.getClass().equals(ROPCFlowProvider.class)) {
                        provider = poolProvider;
                        break;
                    }
                    break;
            }
        }

        // If all are running, allocate a new one and
        // add it to the POOL
        if (provider == null) {
            switch (flow) {
                case AUTH_CODE:
                    provider = new AuthorizationCodeFlowProvider(controller);
                    break;
                case IMPLICIT:
                    provider = new ImplicitFlowProvider(controller);
                    break;
                case RESOURCE_OWNER_PASSWORD_CREDS:
                    provider = new ROPCFlowProvider(controller);
                    break;
            }

            POOL.add(provider);
        }

        return provider;
    }
}
