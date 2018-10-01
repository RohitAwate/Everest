package com.rohitawate.everest.auth.oauth2.code;

/**
 * Provides a way to display the authorization request screen
 * to the user and retrieve the Authorization Grant for further use.
 */
public interface AuthorizationGrantCapturer {
    /**
     * Presents the Authorization screen and returns the Authorization
     * Grant if the user authorizes the application; null otherwise.
     */
    String getAuthorizationGrant() throws Exception;
}
