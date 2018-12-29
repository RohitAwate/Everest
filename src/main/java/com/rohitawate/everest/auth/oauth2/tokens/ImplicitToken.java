package com.rohitawate.everest.auth.oauth2.tokens;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ImplicitToken extends OAuth2Token {
    @JsonAlias("scope")
    private String scope;

    @JsonAlias("state")
    private String state;

    public ImplicitToken() {
        super();
    }

    @Override
    public String toString() {
        return String.format("ImplicitToken {token: %s, type: %s, expiry: %d, scope: %s, state: %s}",
                accessToken, tokenType, expiresIn, scope, state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ImplicitToken that = (ImplicitToken) o;
        return toString().equals(that.toString());
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
