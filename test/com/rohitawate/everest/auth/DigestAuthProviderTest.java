package com.rohitawate.everest.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DigestAuthProviderTest {

    @Test
    void getAuthHeader() {
        DigestAuthProvider provider = new DigestAuthProvider("https://jigsaw.w3.org/HTTP/Digest/", "GET", "guest", "guest", false);
        System.out.println(provider.getAuthHeader());
    }
}