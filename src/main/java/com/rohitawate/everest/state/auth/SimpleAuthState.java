package com.rohitawate.everest.state.auth;

import java.util.Objects;

public class SimpleAuthState {
    public SimpleAuthState() {
    }

    public SimpleAuthState(String username, String password, boolean enabled) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    public String username;
    public String password;
    public boolean enabled;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleAuthState that = (SimpleAuthState) o;
        return enabled == that.enabled &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password);
    }
}
