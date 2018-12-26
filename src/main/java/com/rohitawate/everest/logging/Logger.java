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

package com.rohitawate.everest.logging;

import com.rohitawate.everest.http.HttpRequest;
import com.rohitawate.everest.misc.EverestUtilities;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;

public class Logger {
    private static ExecutorService executor = EverestUtilities.newDaemonSingleThreadExecutor();

    private static final LogWriter[] coreWriters;
    private static final LogWriter[] serverWriters;

    static {
        Level loggerLevel = Level.INFO;

        coreWriters = new LogWriter[2];
        coreWriters[0] = new CoreHtmlLogWriter(loggerLevel);
        coreWriters[1] = new CoreConsoleLogWriter(loggerLevel);

        serverWriters = new LogWriter[2];
        serverWriters[0] = new ServerFileLogWriter(loggerLevel);
        serverWriters[1] = new ServerConsoleLogWriter(loggerLevel);
    }

    private static final CoreLog CORE_LOG = new CoreLog();
    private static final ServerLog SERVER_LOG = new ServerLog();

    public static void severe(String message, Exception exception) {
        core(message, exception, Level.SEVERE);
    }

    public static void warning(String message, Exception exception) {
        core(message, exception, Level.WARNING);
    }

    public static void info(String message) {
        core(message, null, Level.INFO);
    }

    public static void serverSevere(String serverName, int responseCode, HttpRequest request) {
        server(serverName, responseCode, request, Level.SEVERE);
    }

    public static void serverWarning(String serverName, int responseCode, HttpRequest request) {
        server(serverName, responseCode, request, Level.WARNING);
    }

    public static void serverInfo(String serverName, int responseCode, HttpRequest request) {
        server(serverName, responseCode, request, Level.INFO);
    }

    private static void core(String message, Exception exception, Level level) {
        CORE_LOG.message = message;
        CORE_LOG.exception = exception;
        CORE_LOG.time = LocalDateTime.now();
        CORE_LOG.level = level;
        executor.execute(coreWriteThread);
    }

    private static Runnable coreWriteThread = () -> {
        for (LogWriter writer : coreWriters) {
            writer.append(CORE_LOG);
        }
    };

    private static void server(String serverName, int responseCode, HttpRequest request, Level level) {
        SERVER_LOG.serverName = serverName;
        SERVER_LOG.responseCode = responseCode;
        SERVER_LOG.request = request;
        SERVER_LOG.level = level;
        SERVER_LOG.time = LocalDateTime.now();
        executor.execute(serverWriteThread);
    }

    private static Runnable serverWriteThread = () -> {
        for (LogWriter writer : serverWriters) {
            writer.append(SERVER_LOG);
        }
    };
}
