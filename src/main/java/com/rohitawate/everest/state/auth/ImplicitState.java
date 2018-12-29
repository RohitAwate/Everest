package com.rohitawate.everest.state.auth;

import com.rohitawate.everest.auth.oauth2.tokens.ImplicitToken;

public class ImplicitState extends OAuth2FlowState {
    public String captureMethod;
    public String authURL;
    public String redirectURL;
    public String state;
    public ImplicitToken accessToken;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ImplicitState that = (ImplicitState) o;
        if (!captureMethod.equals(that.captureMethod)) return false;
        if (!authURL.equals(that.authURL)) return false;
        if (!redirectURL.equals(that.redirectURL)) return false;
        if (!state.equals(that.state)) return false;
        if (!accessToken.equals(that.accessToken)) return false;

        return true;
    }
}
