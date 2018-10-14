package com.rohitawate.everest.auth.oauth2.code;

import com.rohitawate.everest.auth.oauth2.AccessToken;
import com.rohitawate.everest.auth.oauth2.OAuth2Provider;
import com.rohitawate.everest.auth.oauth2.code.exceptions.AccessTokenDeniedException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.NoAuthorizationGrantException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.UnknownAccessTokenTypeException;
import com.rohitawate.everest.controllers.auth.oauth2.AuthorizationCodeController.CaptureMethod;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.state.AuthorizationCodeState;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

public class AuthorizationCodeProvider implements OAuth2Provider {
    private URL authURL;
    private URL accessTokenURL;
    private String authGrant;
    private String clientID;
    private String clientSecret;
    private URL redirectURL;
    private String scope;
    private String state;
    private String headerPrefix;
    private boolean enabled;

    private String captureMethod;
    private AccessToken accessToken;

    public AuthorizationCodeProvider(AuthorizationCodeState state) throws MalformedURLException {
        setState(state);
    }

    public void setState(AuthorizationCodeState state) throws MalformedURLException {
        this.authURL = new URL(state.authURL);
        this.accessTokenURL = new URL(state.accessTokenURL);
        this.clientID = state.clientID;
        this.clientSecret = state.clientSecret;
        this.captureMethod = state.grantCaptureMethod;

        if (state.redirectURL == null || state.grantCaptureMethod.equals(CaptureMethod.BROWSER)) {
            this.redirectURL = new URL(BrowserCapturer.LOCAL_SERVER_URL);
        } else {
            this.redirectURL = new URL(state.redirectURL);
        }

        this.scope = state.scope;
        this.state = state.state;

        if (state.headerPrefix == null || state.headerPrefix.isEmpty()) {
            this.headerPrefix = "Bearer";
        } else {
            this.headerPrefix = state.headerPrefix;
        }

        this.accessToken = state.accessToken;
        this.enabled = state.enabled;
    }

    private void fetchAuthorizationGrant() throws Exception {
        StringBuilder grantURLBuilder = new StringBuilder(authURL.toString());
        grantURLBuilder.append("?response_type=code");
        grantURLBuilder.append("&client_id=");
        grantURLBuilder.append(clientID);
        grantURLBuilder.append("&redirect_uri=");
        grantURLBuilder.append(redirectURL.toString());

        if (scope == null || !scope.isEmpty()) {
            grantURLBuilder.append("&scope=");
            grantURLBuilder.append(scope);
        }

        AuthorizationGrantCapturer capturer;
        switch (captureMethod) {
            // TODO: Re-use capturers
            case CaptureMethod.BROWSER:
                capturer = new BrowserCapturer(grantURLBuilder.toString());
                break;
            default:
                capturer = new WebViewCapturer(grantURLBuilder.toString(), redirectURL.toString());
        }

        authGrant = capturer.getAuthorizationGrant();
    }

    private void fetchAccessToken() throws NoAuthorizationGrantException, AccessTokenDeniedException,
            IOException, UnknownAccessTokenTypeException {
        if (authGrant == null) {
            throw new NoAuthorizationGrantException(
                    "OAuth 2.0 Authorization Code: Authorization grant not found. Aborting access token fetch."
            );
        }

        URL tokenURL = new URL(accessTokenURL.toString());
        StringBuilder tokenURLBuilder = new StringBuilder();
        tokenURLBuilder.append("client_id=");
        tokenURLBuilder.append(clientID);
        tokenURLBuilder.append("&client_secret=");
        tokenURLBuilder.append(clientSecret);
        tokenURLBuilder.append("&grant_type=authorization_code");
        tokenURLBuilder.append("&code=");
        tokenURLBuilder.append(authGrant);
        tokenURLBuilder.append("&redirect_uri=");
        tokenURLBuilder.append(redirectURL);
        if (scope != null && !scope.isEmpty()) {
            tokenURLBuilder.append("&scope=");
            tokenURLBuilder.append(scope);
        }

        byte[] postData = tokenURLBuilder.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) tokenURL.openConnection();

        connection.setRequestMethod(HTTPConstants.POST);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Length", String.valueOf(postData.length));
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        connection.getOutputStream().write(postData);

        StringBuilder tokenResponseBuilder = new StringBuilder();
        if (connection.getResponseCode() == 200) {
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNext())
                tokenResponseBuilder.append(scanner.nextLine());
            // Removes the "charset" part
            String contentType = connection.getContentType().split(";")[0];

            switch (contentType) {
                case MediaType.APPLICATION_JSON:
                    accessToken = EverestUtilities.jsonMapper.readValue(tokenResponseBuilder.toString(), AccessToken.class);
                    break;
                case MediaType.APPLICATION_FORM_URLENCODED:
                    accessToken = new AccessToken();

                    HashMap<String, String> params = EverestUtilities.parseParameters(new URL(tokenResponseBuilder.toString()));
                    if (params != null) {
                        params.forEach((key, value) -> {
                            switch (key) {
                                case "access_token":
                                    accessToken.accessToken = value;
                                    break;
                                case "token_type":
                                    accessToken.tokenType = value;
                                    break;
                                case "expires_in":
                                    accessToken.expiresIn = Integer.parseInt(value);
                                    break;
                                case "refresh_token":
                                    accessToken.refreshToken = value;
                                    break;
                                case "scope":
                                    accessToken.scope = value;
                                    break;
                            }
                        });
                    }
                    break;
                default:
                    throw new UnknownAccessTokenTypeException("Unknown access token type: " + contentType + "\nBody: " + tokenResponseBuilder.toString());
            }
        } else {
            System.out.println(connection.getResponseCode());
            Scanner scanner = new Scanner(connection.getErrorStream());
            while (scanner.hasNext())
                tokenResponseBuilder.append(scanner.nextLine());
            throw new AccessTokenDeniedException(tokenResponseBuilder.toString());
        }
    }

    @Override
    public AccessToken getAccessToken() throws Exception {
        if (accessToken == null) {
            getAuthHeader();
        }

        return accessToken;
    }

    @Override
    public String getAuthHeader() throws Exception {
        if (accessToken == null) {
            fetchAuthorizationGrant();
            fetchAccessToken();
        }

        return headerPrefix + " " + accessToken.accessToken;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
