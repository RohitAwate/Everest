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

package com.rohitawate.everest.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Utility class to parse HTTP requests
 */
public class HttpRequest {
    private String method;
    private String path;
    private double version;
    private HashMap<String, String> headers;

    public static HttpRequest parse(InputStream stream, boolean parseHeaders) {
        HttpRequest request = new HttpRequest();
        Scanner scanner = new Scanner(stream);

        while (!scanner.hasNextLine()) ;
        String line = scanner.nextLine();
        String tokens[] = line.split(" ");
        request.method = tokens[0];
        request.path = tokens[1];

        tokens = tokens[2].split("/");
        request.version = Double.parseDouble(tokens[1]);

        if (parseHeaders) {
            request.headers = new HashMap<>();
            while (scanner.hasNextLine() && !(line = scanner.nextLine()).isEmpty()) {
                tokens = line.split(": ");
                request.headers.put(tokens[0], tokens[1]);
            }
        }

        return request;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public double getVersion() {
        return version;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }
}
