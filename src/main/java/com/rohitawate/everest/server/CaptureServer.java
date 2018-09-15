package com.rohitawate.everest.server;

import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.notifications.NotificationsManager;
import javafx.concurrent.Task;

import java.awt.*;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Scanner;

public class CaptureServer extends Task<String> {
    private final int port;
    private String authURL;
    private ServerSocket server;

    private static final String WEB_ROOT = "/html";
    private static final String GRANTED = "/AuthorizationGranted.html";
    private static final String DENIED = "/AuthorizationDenied.html";
    private static final String NOT_FOUND = "/404.html";

    public CaptureServer(int port, String authURL) {
        this.port = port;
        this.authURL = authURL;
    }

    @Override
    public String call() throws Exception {
        String grant;

        server = new ServerSocket(port);
        LoggingService.logInfo("Authorization grant capturing server has started on port " + port + ".", LocalDateTime.now());

        openLinkInBrowser(authURL);
        grant = listen();

        Thread serverShutdownThread = new Thread(() -> {
            try {
                Thread.sleep(10_000);
                server.close();
                LoggingService.logInfo("CaptureServer was shut down.", LocalDateTime.now());
            } catch (IOException e) {
                LoggingService.logSevere("Could not shut down CaptureServer.", e, LocalDateTime.now());
            } catch (InterruptedException e) {
                LoggingService.logSevere("CaptureServer shutdown thread was interrupted.", e, LocalDateTime.now());
            }
        });
        serverShutdownThread.setDaemon(false);
        serverShutdownThread.start();

        new Thread(() -> {
            try {
                while (serverShutdownThread.isAlive()) {
                    listen();
                }
            } catch (IOException e) {
                LoggingService.logSevere("CaptureServer could not serve client.", e, LocalDateTime.now());
            }
        }).start();

        return grant;
    }

    private String listen() throws IOException {
        String grant = null;
        String requestedPath;

        PrintWriter headerWriter;
        DataOutputStream bodyStream;
        try (Socket client = server.accept()) {
            Scanner scanner = new Scanner(client.getInputStream());
            headerWriter = new PrintWriter(client.getOutputStream());
            bodyStream = new DataOutputStream(client.getOutputStream());

            String firstLineTokens[] = scanner.nextLine().split(" ");
            String method = firstLineTokens[0];
            requestedPath = firstLineTokens[1];

            if (method.equals(HTTPConstants.GET)) {
                StringBuilder headers = new StringBuilder("HTTP/1.1 ");
                byte[] body;

                if (requestedPath.startsWith("/granted")) {
                    headers.append("200 OK");

                    String[] params = requestedPath.split("\\?");
                    String error = null;
                    if (params.length > 1) {
                        String pairs[] = params[1].split("&");
                        for (String pair : pairs) {
                            String pairValues[] = pair.split("=");
                            if (pairValues[0].equals("code")) {
                                grant = pairValues[1];
                                break;
                            } else if (pairValues[0].equals("error")) {
                                error = pairValues[1];
                                break;
                            }
                        }
                    }

                    if (grant == null) {
                        String deniedHTML = EverestUtilities.readFile(getClass().getResourceAsStream(WEB_ROOT + DENIED));
                        if (error != null) {
                            deniedHTML = deniedHTML.replace("{% Error %}", error);
                        } else {
                            deniedHTML = deniedHTML.replace("{% Error %}", "Not provided.");
                        }

                        body = deniedHTML.getBytes();
                    } else {
                        body = EverestUtilities.readBytes(getClass().getResourceAsStream(WEB_ROOT + GRANTED));
                    }

                    headers.append("\nContent-Type: text/html");
                } else {
                    try {
                        body = EverestUtilities.readBytes(getClass().getResourceAsStream(WEB_ROOT + requestedPath));
                        headers.append("200 OK");
                        headers.append("\nContent-Type: ");
                        headers.append(getMimeType(requestedPath));
                    } catch (FileNotFoundException e) {
                        body = EverestUtilities.readBytes(getClass().getResourceAsStream(WEB_ROOT + NOT_FOUND));
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

        if (requestedPath != null) {

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
