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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AccessToken that = (AccessToken) obj;
        if (!this.accessToken.equals(that.accessToken)) return false;
        if (!this.tokenType.equals(that.tokenType)) return false;
        if (this.expiresIn != that.expiresIn) return false;
        if (!this.refreshToken.equals(that.refreshToken)) return false;
        if (!this.scope.equals(that.scope)) return false;
        if (!this.idToken.equals(that.idToken)) return false;

        return true;
    }
}
