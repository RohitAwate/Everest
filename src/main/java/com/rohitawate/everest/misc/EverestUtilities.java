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

package com.rohitawate.everest.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.net.UrlEscapers;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.notifications.NotificationsManager;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class EverestUtilities {
    public static ObjectMapper jsonMapper;

    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        jsonMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Removes leading and trailing quotation marks from strings.
     *
     * @param input String with leading and trailing quotation marks.
     * @return trimmedString - String with leading and trailing quotation marks removed.
     */
    public static String trimString(String input) {
        return input.replaceAll("\"", "");
    }

    public static String encodeURL(String url) {
        return UrlEscapers.urlFragmentEscaper().escape(url);
    }

    public static String readFile(InputStream stream) throws IOException {
        if (stream == null) {
            throw new FileNotFoundException();
        }

        StringBuilder builder = new StringBuilder();

        try (Scanner scanner = new Scanner(stream)) {
            while (scanner.hasNext()) {
                builder.append(scanner.nextLine());
                builder.append("\n");
            }
        }

        stream.close();
        return builder.toString();
    }

    public static byte[] readBytes(InputStream stream) throws IOException {
        if (stream == null) {
            throw new FileNotFoundException();
        }

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int byteRead;

        while ((byteRead = stream.read()) != -1)
            outStream.write(byteRead);

        return outStream.toByteArray();
    }

    public static HashMap<String, String> parseParameters(URL url) {
        String pair[] = url.toString().split("\\?");

        if (pair.length != 2) {
            return null;
        }

        HashMap<String, String> params = null;
        String paramPairs[] = pair[1].split("&");
        for (String paramPair : paramPairs) {
            pair = paramPair.split("=");

            if (pair.length == 2) {
                if (params == null) {
                    params = new HashMap<>();
                }

                params.put(pair[0], pair[1]);
            }
        }

        return params;
    }

    public static ExecutorService newDaemonSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public static void openLinkInBrowser(String url) {
        if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    Logger.warning("Invalid URL encountered while opening link in browser.", ex);
                }
            }).start();

            Logger.info("Opened " + url + " in system browser.");
        } else {
            NotificationsManager.push("Couldn't find a web browser on your system.", 6000);
        }
    }
}
