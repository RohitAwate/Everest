package com.rohitawate.everest.auth.oauth2;

import com.fasterxml.jackson.annotation.JsonAlias;

public class AccessToken {
    @JsonAlias("access_token")
    public String accessToken;

    @JsonAlias("token_type")
    public String tokenType;

    @JsonAlias("expires_in")
    public int expiresIn;

    @JsonAlias("refresh_token")
    public String refreshToken;

    @JsonAlias("scope")
    public String scope;

    @JsonAlias("id_token")
    public String idToken;

    @Override
    public String toString() {
        return "AccessToken{" +
                "accessToken='" + accessToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", tokenExpiry=" + expiresIn +
                ", refreshToken='" + refreshToken + '\'' +
                ", scope='" + scope + '\'' +
                ", idToken='" + idToken + '\'' +
                '}';
    }
}
