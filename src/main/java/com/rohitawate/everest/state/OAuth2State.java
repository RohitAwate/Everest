package com.rohitawate.everest.state;

public class OAuth2State {
    public AuthorizationCodeState codeState;

    public OAuth2State() {
    }

    public OAuth2State(AuthorizationCodeState codeState) {
        this.codeState = codeState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OAuth2State that = (OAuth2State) o;
        if (!codeState.equals(that.codeState)) return false;

        return true;
    }
}
