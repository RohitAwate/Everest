package com.rohitawate.everest.server.mock;

import com.rohitawate.everest.Main;
import com.rohitawate.everest.http.HttpRequestParser;
import com.rohitawate.everest.models.responses.EverestResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class WebService {
    private String identifier;
    private ArrayList<Endpoint> endpoints;

    private boolean prefixIdentifier;

    public WebService(String identifier, boolean prefixIdentifier) {
        setIdentifier(identifier);
        this.prefixIdentifier = prefixIdentifier;
        this.endpoints = new ArrayList<>();
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier.toLowerCase().replaceAll(" ", "").trim();
    }

    public void addEndpoint(Endpoint endpoint) {
        this.endpoints.add(endpoint);
    }

    String getIdentifier() {
        return this.identifier;
    }

    private String stripIdentifier(String path) {
        if (path.startsWith("/" + identifier))
            return path.substring(identifier.length() + 1);

        return path;
    }

    void handle(Socket socket, HttpRequestParser requestParser) throws IOException {
        String path = prefixIdentifier ? stripIdentifier(requestParser.getPath()) : requestParser.getPath();

        for (Endpoint endpoint : endpoints) {
            if (endpoint.path.equals(path)) {
                sendResponse(socket, endpoint);
                ServerLogger.logInfo(endpoint.responseCode, requestParser);
                return;
            }
        }

        MockServer.handleNotFound(socket, requestParser);
    }

    static void sendResponse(Socket socket, Endpoint endpoint) throws IOException {
        PrintWriter headersWriter = new PrintWriter(socket.getOutputStream());
        DataOutputStream bodyStream = new DataOutputStream(socket.getOutputStream());

        headersWriter.println(generateHeader(endpoint.responseCode, endpoint.serializationFormat, endpoint.resource.length()));
        headersWriter.flush();

        bodyStream.write(endpoint.resource.getBytes(), 0, endpoint.resource.length());
        bodyStream.flush();

        headersWriter.close();
        bodyStream.close();
        socket.close();
    }

    private static String generateHeader(int statusCode, String contentType, int contentLength) {
        StringBuffer header = new StringBuffer("HTTP/1.1 ");
        header.append(statusCode);
        header.append(" ");
        header.append(EverestResponse.getReasonPhrase(statusCode));

        header.append("\nContent-Type: ");
        header.append(contentType);

        header.append("\nContent-Length: ");
        header.append(contentLength);

        header.append("\nServer: ");
        header.append(Main.APP_NAME);
        header.append("/MockServer\n");

        return header.toString();
    }

    public void setPrefixIdentifier(boolean prefixIdentifier) {
        this.prefixIdentifier = prefixIdentifier;
    }

    public boolean isPrefixIdentifier() {
        return prefixIdentifier;
    }
}
