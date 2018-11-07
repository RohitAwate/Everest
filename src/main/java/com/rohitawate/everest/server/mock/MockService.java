package com.rohitawate.everest.server.mock;

import com.rohitawate.everest.http.HttpRequestParser;
import com.rohitawate.everest.logging.LoggingService;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class MockService implements Runnable {
    private ServerSocket server;
    private boolean running;
    private String prefix;
    private boolean attachPrefix;

    public boolean loggingEnabled;
    public String name;

    private ArrayList<Endpoint> endpoints;

    public MockService(String name, int port) throws IOException {
        this.server = new ServerSocket(port);

        this.name = name;
        this.prefix = "";
        this.endpoints = new ArrayList<>();
    }

    public MockService(String name, String prefix, int port) throws IOException {
        this.server = new ServerSocket(port);

        this.name = name;
        this.prefix = prefix;
        this.endpoints = new ArrayList<>();
        this.attachPrefix = prefix != null;
    }

    @Override
    public void run() {
        while (this.running) {
            try {
                Socket socket = this.server.accept();

                Thread requestThread = new Thread(() -> routeRequest(socket));
                requestThread.setDaemon(true);
                requestThread.start();
            } catch (IOException e) {
                LoggingService.logSevere("Mock server could not listen for connections.", e, LocalDateTime.now());
            }
        }
    }

    public void start() {
        this.running = true;

        Thread serverThread = new Thread(this);
        serverThread.setDaemon(true);
        serverThread.start();

        LoggingService.logInfo("Mock server has started on port " + server.getLocalPort() + ".", LocalDateTime.now());
    }

    public void stop() {
        if (running) {
            running = false;
            LoggingService.logInfo("Mock server was stopped.", LocalDateTime.now());
        } else {
            LoggingService.logInfo("Mock server is not running.", LocalDateTime.now());
        }
    }

    private void routeRequest(Socket socket) {
        try {
            HttpRequestParser requestParser = new HttpRequestParser(socket.getInputStream(), false);

            if (requestParser.getPath().startsWith(this.prefix) || attachPrefix) {
                String path = stripPrefix(requestParser.getPath());

                for (Endpoint endpoint : endpoints) {
                    if (path.equals(endpoint.path)) {
                        ResponseWriter.sendResponse(socket, endpoint);
                        ServerLogger.logInfo(this.name, endpoint.responseCode, requestParser);
                        return;
                    }
                }
            }

            handleNotFound(socket, requestParser);
        } catch (IOException e) {
            LoggingService.logSevere("Error while routing request.", e, LocalDateTime.now());
        }
    }

    public void addEndpoint(Endpoint endpoint) {
        this.endpoints.add(endpoint);
    }

    public void removeEndpoint(Endpoint endpoint) {
        this.endpoints.remove(endpoint);
    }

    private String stripPrefix(String path) {
        if (prefix.length() < path.length()) {
            return path.substring(prefix.length());
        }

        return path;
    }

    private static Endpoint notFound = new Endpoint(null, null, 404,
            "{ \"error\": \"Requested route does not support this method or does not exist.\"}", MediaType.APPLICATION_JSON);

    private void handleNotFound(Socket socket, HttpRequestParser requestParser) throws IOException {
        ResponseWriter.sendResponse(socket, notFound);
        ServerLogger.logWarning(this.name, 404, requestParser);
    }

    public boolean isRunning() {
        return running;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isAttachPrefix() {
        return attachPrefix;
    }

    public void setAttachPrefix(boolean attachPrefix) {
        this.attachPrefix = attachPrefix;
    }
}
