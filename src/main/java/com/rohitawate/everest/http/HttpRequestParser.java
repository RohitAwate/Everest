package com.rohitawate.everest.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

public class HttpRequestParser {
    private String method;
    private String path;
    private double version;
    private HashMap<String, String> headers;

    public HttpRequestParser(InputStream stream, boolean parseHeaders) {
        Scanner scanner = new Scanner(stream);

        String line = scanner.nextLine();
        String tokens[] = line.split(" ");
        this.method = tokens[0];
        this.path = tokens[1];

        tokens = tokens[2].split("/");
        this.version = Double.parseDouble(tokens[1]);

        if (parseHeaders) {
            this.headers = new HashMap<>();
            while (!(line = scanner.nextLine()).isEmpty()) {
                tokens = line.split(": ");
                this.headers.put(tokens[0], tokens[1]);
            }
        }
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
