package com.rohitawate.everest.auth.oauth2;

import com.rohitawate.everest.auth.AuthProvider;

/**
 * Adds OAuth 2.0-specific functionality to AuthProvider.
 */
public interface OAuth2Provider extends AuthProvider {
    /**
     * Returns the access token for the respective API.
     */
    AccessToken getAccessToken();
}
