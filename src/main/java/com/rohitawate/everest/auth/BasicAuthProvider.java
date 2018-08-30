package com.rohitawate.everest.auth;

import java.util.Base64;
import java.util.Base64.Encoder;

public class BasicAuthProvider implements AuthProvider {
    private static Encoder encoder = Base64.getEncoder();

    private String username;
    private String password;
    private boolean enabled;

    public BasicAuthProvider(String username, String password, boolean enabled) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    @Override
    public String getAuthHeader() {
        return "Basic " + encoder.encodeToString((username + ":" + password).getBytes());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
