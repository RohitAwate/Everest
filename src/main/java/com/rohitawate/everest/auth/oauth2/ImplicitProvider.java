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

import com.rohitawate.everest.auth.captors.WebViewCaptor;
import com.rohitawate.everest.auth.oauth2.exceptions.AuthWindowClosedException;
import com.rohitawate.everest.auth.oauth2.tokens.ImplicitToken;
import com.rohitawate.everest.controllers.auth.oauth2.ImplicitController;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.state.auth.ImplicitState;
import com.rohitawate.everest.state.auth.OAuth2FlowState;

/*
 * Please refer the comment above the class declaration for:
 * com.rohitawate.everest.controllers.auth.oauth2.ImplicitController
 *
 * Due to the reasons mentioned there, the BrowserCaptor is not used here.
 */
public class ImplicitProvider implements OAuth2Provider {
    private ImplicitController controller;
    private ImplicitState state;

    public ImplicitProvider(ImplicitController controller) {
        this.controller = controller;
    }

    @Override
    public ImplicitToken getAccessToken() throws AuthWindowClosedException {
        if (this.state == null) {
            setState(controller.getState());
        }

        StringBuilder builder = new StringBuilder(state.authURL);
        builder.append("?response_type=token");
        builder.append("&client_id=");
        builder.append(state.clientID);
        builder.append("&redirect_uri=");
        builder.append(EverestUtilities.encodeURL(state.redirectURL));

        if (state.scope != null && !state.scope.isBlank()) {
            builder.append("&scope=");
            builder.append(EverestUtilities.encodeURL(state.scope));
        }

        if (state.state != null && !state.state.isBlank()) {
            builder.append("&state=");
            builder.append(EverestUtilities.encodeURL(state.state));
        }

        WebViewCaptor captor = new WebViewCaptor(builder.toString());
        state.accessToken = captor.getImplicitToken();

        // This will display the new AuthCodeToken in the UI
        controller.setAccessToken(state.accessToken);

        return state.accessToken;
    }

    @Override
    public String getAuthHeader() throws Exception {
        if (state == null) {
            setState(controller.getState());
        }

        /*
            Integrated WebView calls will already have been resolved in AuthorizationCodeController,
            hence, they are skipped here.
         */
        if (state.accessToken.getAccessToken().isBlank()) {
            /*
                If there is no AccessToken, it means the view would already have
                opened through ImplicitController, but the user denied
                authorization or closed the window before receiving a accessToken.
             */
            throw new AuthWindowClosedException();
        }

        return String.format("%s %s", state.headerPrefix, state.accessToken.getAccessToken());
    }

    @Override
    public void setState(OAuth2FlowState state) {
        if (state == null) {
            this.state = null;
            return;
        }

        this.state = (ImplicitState) state;

        if (state.headerPrefix == null || state.headerPrefix.isEmpty()) {
            state.headerPrefix = "Bearer";
        }
    }

    @Override
    public boolean isEnabled() {
        // Checking if there has been a change in the state of the enabled checkbox
        setState(controller.getState());
        return state.enabled;
    }

    public void setController(ImplicitController controller) {
        this.controller = controller;
    }
}
