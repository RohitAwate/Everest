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

package com.rohitawate.everest.server.mock;

import com.rohitawate.everest.http.HttpRequestParser;
import com.rohitawate.everest.logging.LoggingService;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MockServer implements Runnable {
    private int port;
    private boolean running;

    private String prefix;
    private boolean attachPrefix;

    public String name;
    public boolean loggingEnabled;
    public int latency;

    private ServerSocket server;
    private ExecutorService executorService;

    private ArrayList<Endpoint> endpoints;

    public MockServer(String name, int port) {
        this.name = name;
        this.prefix = "";
        this.port = port;
        this.endpoints = new ArrayList<>();
        this.latency = 0;
    }

    public MockServer(String name, String prefix, int port) {
        this.name = name;
        this.prefix = prefix;
        this.port = port;
        this.endpoints = new ArrayList<>();
        this.latency = 0;
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

    public void start() throws IOException {
        if (this.server == null) {
            this.server = new ServerSocket(port);
        }

        if (this.executorService == null) {
            this.executorService = Executors.newCachedThreadPool(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    return thread;
                }
            });
        }

        this.running = true;
        executorService.submit(this);

        LoggingService.logInfo("Mock server has started on port " + server.getLocalPort() + ".", LocalDateTime.now());
    }

    public void stop() throws IOException {
        if (running) {
            running = false;
            this.server.close();
            this.server = null;
            LoggingService.logInfo("Mock server was stopped.", LocalDateTime.now());
        } else {
            LoggingService.logInfo("Mock server is not running.", LocalDateTime.now());
        }
    }

    private void routeRequest(Socket socket) {
        try {
            HttpRequestParser requestParser = new HttpRequestParser(socket.getInputStream(), false);

            boolean startsWithPrefix = false;

            if (!this.prefix.isEmpty()) {
                startsWithPrefix = requestParser.getPath().startsWith(this.prefix);
            }

            String path = null;

            if (attachPrefix && startsWithPrefix) {
                path = stripPrefix(requestParser.getPath());
            } else if (!attachPrefix && !startsWithPrefix) {
                path = requestParser.getPath();
            }

            if (path != null) {
                for (Endpoint endpoint : endpoints) {
                    if (path.equals(endpoint.path) && requestParser.getMethod().equals(endpoint.method)) {
                        ResponseWriter.sendResponse(socket, endpoint, latency);
                        if (loggingEnabled) {
                            ServerLogger.logInfo(this.name, endpoint.responseCode, requestParser);
                        }
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
        ResponseWriter.sendResponse(socket, notFound, latency);
        if (loggingEnabled) {
            ServerLogger.logWarning(this.name, 404, requestParser);
        }
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

    public ArrayList<Endpoint> getEndpoints() {
        return this.endpoints;
    }

    public int getPort() {
        return port;
    }
}
