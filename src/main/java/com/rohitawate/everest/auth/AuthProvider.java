package com.rohitawate.everest.auth;

public interface AuthProvider {
    /**
     * Returns true or false indicating whether or not the user has enabled authentication.
     */
    boolean isEnabled();

    /**
     * Returns the 'Authorization' header to be attached to an API call.
     */
    String getAuthHeader();
}
