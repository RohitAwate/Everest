/*
 * Copyright 2019 Rohit Awate.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rohitawate.everest.server;

import com.rohitawate.everest.http.HttpRequest;
import com.rohitawate.everest.http.HttpResponse;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.models.requests.HTTPConstants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;

public class CaptureServer {
    private static ServerSocket server;
    private static final int PORT = 52849;

    private static final String WEB_ROOT = "/html";
    private static final String GRANTED = "/AuthorizationGranted.html";
    private static final String DENIED = "/AuthorizationDenied.html";
    private static final String NOT_FOUND = "/404.html";

    private static String redirectURL;

    public static String captureAuthorizationCode(String authURL) throws Exception {
        startServer();

        EverestUtilities.openLinkInBrowser(authURL);
        redirectURL = null;
        return listenForGrant();
    }

    private static void startServer() {
        if (server == null) {
            try {
                server = new ServerSocket(PORT);
                Logger.info("Capture server has started on port " + PORT + ".");
            } catch (IOException e) {
                Logger.severe("Could not start capture server on port " + PORT + ".", e);
            }
        }
    }

    /**
     * Starts listening for connection requests for the Authorization Code
     * flow of OAuth 2.0. It expects the authorization code grant in the URL.
     *
     * @return The authorization code grant issued by the server.
     * @throws IOException - If any error is encountered while running the server.
     */
    private static String listenForGrant() throws IOException {
        Socket client = server.accept();

        String grant = null;
        int status;
        byte[] body;
        String contentType;

        HttpRequest request = HttpRequest.parse(client.getInputStream(), false);
        if (request.getMethod().equals(HTTPConstants.GET)) {
            if (!request.getPath().startsWith("/granted")) {
                handleStatic(client, request);
                return null;
            }

            redirectURL = "http://localhost:52849" + request.getPath();
            HashMap<String, String> params = EverestUtilities.parseParameters(new URL(redirectURL), "\\?");

            String error = null;
            if (params != null) {
                grant = params.get("code");
                error = params.get("error");
            }

            if (grant == null) {
                String denied = EverestUtilities.readFile(CaptureServer.class.getResourceAsStream(WEB_ROOT + DENIED));
                if (error != null) {
                    denied = denied.replace("{% Error %}", error);
                } else {
                    denied = denied.replace("{% Error %}", "Not provided.");
                }

                body = denied.getBytes();
            } else {
                body = EverestUtilities.readBytes(CaptureServer.class.getResourceAsStream(WEB_ROOT + GRANTED));
            }

            status = 200;
            contentType = "text/html";
        } else {
            body = "Method not allowed".getBytes();
            status = 405;
            contentType = "text/plain";
        }

        HttpResponse response = new HttpResponse(status, body, contentType);
        response.write(client);
        client.close();

        return grant;
    }

    private static void handleStatic(Socket client, HttpRequest request) throws IOException {
        int status;
        byte[] body;
        String contentType;

        try {
            body = EverestUtilities.readBytes(CaptureServer.class.getResourceAsStream(WEB_ROOT + request.getPath()));
            status = 200;
            contentType = getMimeType(request.getPath());
        } catch (FileNotFoundException e) {
            body = EverestUtilities.readBytes(CaptureServer.class.getResourceAsStream(WEB_ROOT + NOT_FOUND));
            status = 404;
            contentType = "text/html";
        }

        HttpResponse response = new HttpResponse(status, body, contentType);
        response.write(client);
        client.close();
    }

    private static String getMimeType(String file) {
        String[] tokens = file.split("\\.");
        if (tokens.length > 1) {
            switch (tokens[1]) {
                case "html":
                    return "text/html";
                case "png":
                    return "image/png";
                case "ico":
                    return "image/x-icon";
                case "jpeg":
                    return "image/jpeg";
                default:
                    return "text/plain";
            }
        } else {
            return "text/plain";
        }
    }
}
