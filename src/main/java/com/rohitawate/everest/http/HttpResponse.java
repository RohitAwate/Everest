/*
 * Copyright 2019 Rohit Awate.
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

package com.rohitawate.everest.http;

import com.rohitawate.everest.models.responses.EverestResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class HttpResponse {
    private int statusCode;
    private byte[] body;
    private HashMap<String, String> headers;

    public HttpResponse(int statusCode, byte[] body, String contentType) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>();
        setHeader("Content-Type", contentType);
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public byte[] getBody() {
        return body;
    }

    public void write(Socket client) throws IOException {
        PrintWriter headerWriter = new PrintWriter(client.getOutputStream());
        DataOutputStream bodyStream = new DataOutputStream(client.getOutputStream());

        headerWriter.println("HTTP/1.1 ");
        headerWriter.println(String.format("%d %s", statusCode, EverestResponse.getReasonPhrase(statusCode)));

        setHeader("Content-Length", String.valueOf(body.length));
        headers.forEach((key, value) -> headerWriter.println(String.format("%s: %s", key, value)));
        headerWriter.println();     // Line-break between end of headers and body
        headerWriter.flush();

        bodyStream.write(body, 0, body.length);
        bodyStream.flush();

        headerWriter.close();
        bodyStream.close();
        client.close();
    }
}
