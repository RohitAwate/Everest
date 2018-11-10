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

import com.rohitawate.everest.Main;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.models.responses.EverestResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

public class ResponseWriter {
    static void sendResponse(Socket socket, Endpoint endpoint, int latency) throws IOException {
        PrintWriter headersWriter = new PrintWriter(socket.getOutputStream());
        DataOutputStream bodyStream = new DataOutputStream(socket.getOutputStream());

        try {
            if (endpoint.latency + latency > 0) {
                Thread.sleep(endpoint.latency + latency);
            }

            headersWriter.println(generateHeader(endpoint.responseCode, endpoint.contentType, endpoint.resource.length()));
            headersWriter.flush();

            bodyStream.write(endpoint.resource.getBytes(), 0, endpoint.resource.length());
            bodyStream.flush();
        } catch (InterruptedException e) {
            LoggingService.logSevere("Thread interrupted during latency period.", e, LocalDateTime.now());
        } finally {
            headersWriter.close();
            bodyStream.close();
            socket.close();
        }
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
