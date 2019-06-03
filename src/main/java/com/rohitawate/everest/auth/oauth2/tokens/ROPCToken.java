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

package com.rohitawate.everest.auth.oauth2.tokens;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ROPCToken extends OAuth2Token {
    @JsonAlias("refresh_token")
    private String refreshToken;

    @JsonAlias("scope")
    private String scope;

    public ROPCToken() {
        super();
    }

    @Override
    public String toString() {
        return "ROPCToken{" +
                "refreshToken='" + refreshToken + '\'' +
                ", scope='" + scope + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
