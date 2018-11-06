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

    public WebService(String identifier) {
        setIdentifier(identifier);
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

    void handle(Socket socket, HttpRequestParser requestParser) throws IOException {
        PrintWriter headersWriter = new PrintWriter(socket.getOutputStream());
        DataOutputStream bodyStream = new DataOutputStream(socket.getOutputStream());

        for (Endpoint endpoint : endpoints) {
            if (endpoint.path.equals(requestParser.getPath())) {
                headersWriter.println(generateHeader(endpoint.responseCode, endpoint.serializationFormat, endpoint.resource.length()));
                headersWriter.flush();

                bodyStream.write(endpoint.resource.getBytes(), 0, endpoint.resource.length());
                bodyStream.flush();

                headersWriter.close();
                bodyStream.close();
                socket.close();

                return;
            }
        }

        MockServer.routeNotFound(socket, requestParser);
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
}
