package com.rohitawate.everest.state.auth;

public class OAuth2FlowState {
    public String clientID;
    public boolean enabled;
    public String scope;
    public String headerPrefix;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthorizationCodeState that = (AuthorizationCodeState) o;
        if (enabled != that.enabled) return false;
        if (!clientID.equals(that.clientID)) return false;
        if (!scope.equals(that.scope)) return false;
        if (!headerPrefix.equals(that.headerPrefix)) return false;

        return true;
    }
}
