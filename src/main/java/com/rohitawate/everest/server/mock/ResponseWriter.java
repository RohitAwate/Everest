package com.rohitawate.everest.server.mock;

import com.rohitawate.everest.Main;
import com.rohitawate.everest.models.responses.EverestResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ResponseWriter {
    static void sendResponse(Socket socket, Endpoint endpoint) throws IOException {
        PrintWriter headersWriter = new PrintWriter(socket.getOutputStream());
        DataOutputStream bodyStream = new DataOutputStream(socket.getOutputStream());

        headersWriter.println(generateHeader(endpoint.responseCode, endpoint.contentType, endpoint.resource.length()));
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
}
