package com.rohitawate.everest.server;

import com.rohitawate.everest.http.HttpRequestParser;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.notifications.NotificationsManager;

import java.awt.*;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;

public class CaptureServer {
    private static ServerSocket server;
    private static final int PORT = 52849;

    private static final String WEB_ROOT = "/html";
    private static final String GRANTED = "/AuthorizationGranted.html";
    private static final String DENIED = "/AuthorizationDenied.html";
    private static final String NOT_FOUND = "/404.html";

    public static String capture(String authURL) throws Exception {
        if (server == null) {
            try {
                server = new ServerSocket(PORT);
                LoggingService.logInfo("Authorization grant capturing server has started on port " + PORT + ".", LocalDateTime.now());
            } catch (IOException e) {
                LoggingService.logSevere("Could not start capture server on port " + PORT + ".", e, LocalDateTime.now());
            }
        }

        openLinkInBrowser(authURL);
        return listen();
    }

    private static String listen() throws IOException {
        String grant = null;

        PrintWriter headerWriter;
        DataOutputStream bodyStream;
        try (Socket client = server.accept()) {
            HttpRequestParser requestParser = new HttpRequestParser(client.getInputStream(), false);
            headerWriter = new PrintWriter(client.getOutputStream());
            bodyStream = new DataOutputStream(client.getOutputStream());

            if (requestParser.getMethod().equals(HTTPConstants.GET)) {
                StringBuilder headers = new StringBuilder("HTTP/1.1 ");
                byte[] body;

                if (requestParser.getPath().startsWith("/granted")) {
                    headers.append("200 OK");
                    HashMap<String, String> params = EverestUtilities.parseParameters(new URL("http://localhost:52849" + requestParser.getPath()));

                    String error = null;
                    if (params != null) {
                        grant = params.get("code");
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
                        body = EverestUtilities.readBytes(CaptureServer.class.getResourceAsStream(WEB_ROOT + requestParser.getPath()));
                        headers.append("200 OK");
                        headers.append("\nContent-Type: ");
                        headers.append(getMimeType(requestParser.getPath()));
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

    private static void openLinkInBrowser(String url) {
        if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    LoggingService.logWarning("Invalid URL encountered while opening link in browser.", ex, LocalDateTime.now());
                }
            }).start();

            LoggingService.logInfo("Opened authorization grant page in system browser.", LocalDateTime.now());
        } else {
            NotificationsManager.push("Couldn't find a web browser on your system.", 6000);
        }
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
