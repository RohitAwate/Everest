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

package com.rohitawate.everest.auth.oauth2;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.LocalDateTime;

public class AccessToken {
    @JsonAlias("access_token")
    private String accessToken;

    @JsonAlias("token_type")
    private String tokenType;

    @JsonAlias("expires_in")
    private int expiresIn;

    @JsonProperty
    private LocalDateTime tokenCreationTime;

    @JsonAlias("refresh_token")
    private String refreshToken;

    @JsonAlias("scope")
    private String scope;

    public AccessToken() {
        this.tokenCreationTime = LocalDateTime.now();
    }

    public AccessToken(String accessToken, String tokenType, int expiresIn, String refreshToken, String scope) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.tokenCreationTime = LocalDateTime.now();
        this.refreshToken = refreshToken;
        this.scope = scope;
    }

    @Override
    public String toString() {
        return String.format("AccessToken {token: %s, type: %s, expiry: %d, refreshToken: %s, scope: %s}",
                accessToken, tokenType, expiresIn, refreshToken, scope);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AccessToken that = (AccessToken) obj;
        return toString().equals(that.toString());
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @JsonIgnore
    public long getTimeToExpiry() {
        Duration duration = Duration.between(this.tokenCreationTime, LocalDateTime.now());
        return this.expiresIn - duration.getSeconds();
    }

    public boolean hasExpired() {
        return getTimeToExpiry() > this.expiresIn;
    }
}
