package com.rohitawate.everest.server.mock;

import com.rohitawate.everest.http.HttpRequestParser;
import com.rohitawate.everest.logging.LoggingService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class MockServer implements Runnable {
    private static final int PORT = 9090;
    private ServerSocket server;
    private boolean isRunning;

    private ArrayList<WebService> webServices;

    public MockServer() throws IOException {
        this.server = new ServerSocket(PORT);
        this.webServices = new ArrayList<>();
    }

    public void start() {
        this.isRunning = true;

        Thread serverThread = new Thread(this);
        serverThread.setDaemon(true);
        serverThread.start();

        LoggingService.logInfo("Mock server has started on port " + PORT + ".", LocalDateTime.now());
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
        } else {
            LoggingService.logInfo("Mock server is not running.", LocalDateTime.now());
        }
    }

    private static String getIdentifierFromPath(String path) {
        String parts[] = path.split("/");
        if (parts.length > 1) {
            return parts[1];
        } else {
            return path;
        }
    }

    private void routeRequest(Socket socket) {
        try {
            HttpRequestParser requestParser = new HttpRequestParser(socket.getInputStream(), true);
            String identifier = getIdentifierFromPath(requestParser.getPath());

            for (WebService service : webServices) {
                if (service.getIdentifier().equals(identifier)) {
                    service.handle(socket, requestParser);
                    return;
                }
            }

            routeNotFound(socket, requestParser);
        } catch (IOException e) {
            LoggingService.logSevere("Could not parse request: Socket error.", e, LocalDateTime.now());
        }
    }

    static void routeNotFound(Socket socket, HttpRequestParser requestParser) {
        System.err.println("404 Not Found: " + requestParser.getPath());
    }

    public void addService(WebService webService) {
        this.webServices.add(webService);
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                Socket socket = this.server.accept();

                Thread requestThread = new Thread(() -> routeRequest(socket));
                requestThread.setDaemon(true);
                requestThread.start();
            } catch (IOException e) {
                LoggingService.logSevere("Mock server could not listen for connections.", e, LocalDateTime.now());
            }
        }

        try {
            server.close();
        } catch (IOException e) {
            LoggingService.logSevere("Could not shut down mock server.", e, LocalDateTime.now());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
