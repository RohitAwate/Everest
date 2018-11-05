package com.rohitawate.everest.state;

import com.rohitawate.everest.state.auth.OAuth2State;
import com.rohitawate.everest.state.auth.SimpleAuthState;

public class AuthState {
    public SimpleAuthState basicAuthState;
    public SimpleAuthState digestAuthState;
    public OAuth2State oAuth2State;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthState state = (AuthState) o;

        if (!basicAuthState.equals(state.basicAuthState)) return false;
        if (!digestAuthState.equals(state.digestAuthState)) return false;
        if (!oAuth2State.equals(state.oAuth2State)) return false;

        return true;
    }
}
