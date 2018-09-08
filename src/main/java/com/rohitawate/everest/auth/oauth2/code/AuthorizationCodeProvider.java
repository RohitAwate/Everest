package com.rohitawate.everest.auth.oauth2.code;

import com.rohitawate.everest.auth.AuthProvider;
import com.rohitawate.everest.auth.oauth2.code.exceptions.AccessTokenDeniedException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.NoAuthorizationGrantException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.UnknownAccessTokenTypeException;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.models.requests.HTTPConstants;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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

    public enum CaptureMethod {
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

    private void getAuthorizationGrant() throws Exception {
        StringBuilder grantURLBuilder = new StringBuilder(authURL.toString());
        grantURLBuilder.append("?response_type=code");
        grantURLBuilder.append("&client_id=");
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
                capturer = new WebViewCapturer(grantURLBuilder.toString(), redirectURL.toString());
        }

        authGrant = capturer.getAuthorizationGrant();
    }

    private String getAccessToken() throws NoAuthorizationGrantException, AccessTokenDeniedException,
            IOException, UnknownAccessTokenTypeException {
        if (authGrant == null) {
            throw new NoAuthorizationGrantException(
                    "OAuth 2.0 Authorization Code: Authorization grant not found. Aborting access token fetch."
            );
        }

        AccessToken accessToken;
        URL tokenURL = new URL(accessTokenURL.toString());
        String tokenURLBuilder = "client_id=" +
                clientID +
                "&client_secret=" +
                clientSecret +
                "&grant_type=authorization_code" +
                "&code=" +
                authGrant +
                "&redirect_uri=" +
                redirectURL;

        byte[] postData = tokenURLBuilder.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) tokenURL.openConnection();
        connection.setRequestMethod(HTTPConstants.POST);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Length", String.valueOf(postData.length));
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        connection.getOutputStream().write(postData);

        StringBuilder tokenResponseBuilder = new StringBuilder();
        Scanner scanner = new Scanner(connection.getInputStream());
        while (scanner.hasNext())
            tokenResponseBuilder.append(scanner.nextLine());

        if (connection.getResponseCode() == 200) {
            // Removes the "charset" part
            String contentType = connection.getContentType().split(";")[0];

            switch (contentType) {
                case MediaType.APPLICATION_JSON:
                    accessToken = EverestUtilities.jsonMapper.readValue(tokenResponseBuilder.toString(), AccessToken.class);
                    break;
                case MediaType.APPLICATION_FORM_URLENCODED:
                    String key, value;
                    accessToken = new AccessToken();

                    for (String pair : tokenResponseBuilder.toString().split("&")) {
                        if (pair.split("=").length == 2) {
                            key = pair.split("=")[0];
                            value = pair.split("=")[1];
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
                        }
                    }
                    break;
                default:
                    throw new UnknownAccessTokenTypeException("Unknown access token type: " + contentType + "\nBody: " + tokenResponseBuilder.toString());
            }
            // TODO: Save the access token
        } else {
            throw new AccessTokenDeniedException(tokenResponseBuilder.toString());
        }

        return accessToken.accessToken;
    }

    @Override
    public String getAuthHeader() throws Exception {
        getAuthorizationGrant();
        return "Bearer " + getAccessToken();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
