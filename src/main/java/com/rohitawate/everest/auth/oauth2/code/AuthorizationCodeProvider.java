package com.rohitawate.everest.auth.oauth2.code;

import com.rohitawate.everest.auth.AuthProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AuthorizationCodeProvider implements AuthProvider {

    private URL authURL;
    private URL accessTokenURL;
    private String authGrant;
    private String clientID;
    private String clientSecret;
    private URL redirectURL;
    private String scope;
    private boolean enabled;
    private CaptureMethod captureMethod;

    private static final String LOCAL_SERVER_URL = "http://localhost:52849/granted";

    enum CaptureMethod {
        WEB_VIEW, BROWSER
    }

    public AuthorizationCodeProvider(String authURL,
                                     String accessTokenURL, String clientID,
                                     String clientSecret, String redirectURL,
                                     String scope,
                                     boolean enabled,
                                     CaptureMethod captureMethod) throws MalformedURLException {
        this.authURL = new URL(authURL);
        this.accessTokenURL = new URL(accessTokenURL);
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.captureMethod = captureMethod;
        if (redirectURL == null)
            this.redirectURL = new URL(LOCAL_SERVER_URL);
        else
            this.redirectURL = new URL(redirectURL);
        this.scope = scope;
        this.enabled = enabled;
    }

    private String getAuthorizationGrant() {
        StringBuilder grantURLBuilder = new StringBuilder(authURL.toString());
        grantURLBuilder.append("?response_type=code&");
        grantURLBuilder.append("client_id=");
        grantURLBuilder.append(clientID);
        grantURLBuilder.append("&redirect_uri=");
        grantURLBuilder.append(redirectURL.toString());

        if (scope != null) {
            grantURLBuilder.append("&scope=");
            grantURLBuilder.append(scope);
        }

        AuthorizationGrantCapturer capturer;
        switch (captureMethod) {
            case BROWSER:
                capturer = new BrowserCapturer();
                break;
            default:
                capturer = new WebViewCapturer(grantURLBuilder.toString());
        }

        return capturer.getAuthorizationGrant();
    }

    private String getAccessToken() {
        try {
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection conn = (HttpURLConnection) accessTokenURL.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("client_id", clientID);
            conn.setRequestProperty("client_secret", clientSecret);
            conn.setRequestProperty("grant_type", "authorization_code");
            conn.setRequestProperty("code", authGrant);
            conn.setRequestProperty("redirect_uri", redirectURL.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getAuthHeader() {
        getAuthorizationGrant();
        return getAccessToken();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
