/*
 * Copyright 2018 Rohit Awate.
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
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.models.requests.HTTPConstants;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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

    public static String capture(String authURL, String captureKey) throws Exception {
        if (server == null) {
            try {
                server = new ServerSocket(PORT);
                Logger.info("Authorization grant capturing server has started on port " + PORT + ".");
            } catch (IOException e) {
                Logger.severe("Could not start capture server on port " + PORT + ".", e);
            }
        }

        EverestUtilities.openLinkInBrowser(authURL);
        redirectURL = null;
        return listen(captureKey);
    }

    private static String listen(String captureKey) throws IOException {
        String grant = null;

        PrintWriter headerWriter;
        DataOutputStream bodyStream;
        try (Socket client = server.accept()) {
            HttpRequest request = HttpRequest.parse(client.getInputStream(), false);
            headerWriter = new PrintWriter(client.getOutputStream());
            bodyStream = new DataOutputStream(client.getOutputStream());

            if (request.getMethod().equals(HTTPConstants.GET)) {
                StringBuilder headers = new StringBuilder("HTTP/1.1 ");
                byte[] body;

                if (request.getPath().startsWith("/granted")) {
                    headers.append("200 OK");
                    redirectURL = "http://localhost:52849" + request.getPath();
                    HashMap<String, String> params = EverestUtilities.parseParameters(new URL(redirectURL));

                    String error = null;
                    if (params != null) {
                        grant = params.get(captureKey);
                        error = params.get("error");
                    }

                    if (grant == null) {
                        String deniedHTML = EverestUtilities.readFile(CaptureServer.class.getResourceAsStream(WEB_ROOT + DENIED));
                        if (error != null) {
                            deniedHTML = deniedHTML.replace("{% Error %}", error);
                        } else {
                            deniedHTML = deniedHTML.replace("{% Error %}", "Not provided.");
                        }

                        body = deniedHTML.getBytes();
                    } else {
                        body = EverestUtilities.readBytes(CaptureServer.class.getResourceAsStream(WEB_ROOT + GRANTED));
                    }

                    headers.append("\nContent-Type: text/html");
                } else {
                    try {
                        body = EverestUtilities.readBytes(CaptureServer.class.getResourceAsStream(WEB_ROOT + request.getPath()));
                        headers.append("200 OK");
                        headers.append("\nContent-Type: ");
                        headers.append(getMimeType(request.getPath()));
                    } catch (FileNotFoundException e) {
                        body = EverestUtilities.readBytes(CaptureServer.class.getResourceAsStream(WEB_ROOT + NOT_FOUND));
                        headers.append("404 Not Found");
                        headers.append("\nContent-Type: text/html");
                    }
                }

                headers.append("\nContent-Length: ");
                headers.append(body.length);
                headers.append("\n");

                headerWriter.println(headers.toString());
                headerWriter.flush();

                bodyStream.write(body, 0, body.length);
                bodyStream.flush();
            } else {
                System.out.println("Not supported.");
            }
        }

        return grant;
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

    public static String getRedirectURL() {
        return redirectURL;
    }
}
