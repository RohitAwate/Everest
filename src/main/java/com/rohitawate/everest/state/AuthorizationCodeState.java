package com.rohitawate.everest.state;

public class AuthorizationCodeState {

    public AuthorizationCodeState() {
    }

    public AuthorizationCodeState(String authURL, String accessTokenURL, String redirectURL, String clientID,
                                  String clientSecret, String scope, String state, String headerPrefix,
                                  String accessToken, String refreshToken, int expiresIn, boolean enabled) {
        this.authURL = authURL;
        this.accessTokenURL = accessTokenURL;
        this.redirectURL = redirectURL;
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.state = state;
        this.headerPrefix = headerPrefix;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.enabled = enabled;
    }

    public String authURL;
    public String accessTokenURL;
    public String redirectURL;

    public String clientID;
    public String clientSecret;

    public String scope;
    public String state;
    public String headerPrefix;

    public String accessToken;
    public String refreshToken;
    public int expiresIn;

    public boolean enabled;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthorizationCodeState that = (AuthorizationCodeState) o;
        if (!authURL.equals(that.authURL)) return false;
        if (!accessTokenURL.equals(that.accessTokenURL)) return false;
        if (!redirectURL.equals(that.redirectURL)) return false;
        if (!clientID.equals(that.clientID)) return false;
        if (!clientSecret.equals(that.clientSecret)) return false;
        if (!scope.equals(that.scope)) return false;
        if (!state.equals(that.state)) return false;
        if (!headerPrefix.equals(that.headerPrefix)) return false;
        if (!accessToken.equals(that.accessToken)) return false;
        if (!refreshToken.equals(that.refreshToken)) return false;
        if (expiresIn != that.expiresIn) return false;
        if (enabled != that.enabled) return false;

        return true;
    }
}
