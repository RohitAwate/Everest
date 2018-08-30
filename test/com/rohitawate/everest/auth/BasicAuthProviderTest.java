package com.rohitawate.everest.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicAuthProviderTest {

    @Test
    void getAuthHeader() {
        BasicAuthProvider provider = new BasicAuthProvider("username", "password", true);
        assertEquals("dXNlcm5hbWU6cGFzc3dvcmQ=", provider.getAuthHeader());
    }
}