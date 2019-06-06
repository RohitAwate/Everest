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

package com.rohitawate.everest.state.auth;

import com.rohitawate.everest.auth.oauth2.tokens.ImplicitToken;

public class ImplicitState extends OAuth2FlowState {
    public String authURL;
    public String redirectURL;
    public String state;

    public ImplicitState() {
        accessToken = new ImplicitToken();
    }

    @Override
    public String toString() {
        return "ImplicitState{" +
                ", authURL='" + authURL + '\'' +
                ", redirectURL='" + redirectURL + '\'' +
                ", state='" + state + '\'' +
                ", accessToken=" + accessToken +
                ", clientID='" + clientID + '\'' +
                ", enabled=" + enabled +
                ", scope='" + scope + '\'' +
                ", headerPrefix='" + headerPrefix + '\'' +
                '}';
    }
}
