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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class OAuth2Token {
    @JsonAlias("access_token")
    String accessToken;

    @JsonAlias("token_type")
    String tokenType;

    @JsonAlias("expires_in")
    int expiresIn;

    @JsonProperty
    private LocalDateTime tokenCreationTime;

    OAuth2Token() {
        this.tokenCreationTime = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OAuth2Token that = (OAuth2Token) o;
        if (!accessToken.equals(that.accessToken)) return false;
        if (!tokenType.equals(that.tokenType)) return false;
        if (expiresIn != that.expiresIn) return false;
        if (!tokenCreationTime.equals(that.tokenCreationTime)) return false;

        return true;
    }

    @JsonIgnore
    public long getTimeToExpiry() {
        Duration duration = Duration.between(this.tokenCreationTime, LocalDateTime.now());
        return this.expiresIn - duration.getSeconds();
    }

    public boolean hasExpired() {
        return getTimeToExpiry() > this.expiresIn;
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

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
