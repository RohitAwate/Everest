package com.rohitawate.everest.server.mock;

import com.rohitawate.everest.http.HttpRequestParser;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.models.responses.EverestResponse;

class ServerLogger {
    private static final String logString = "%s[MockServer] %s%s\n";

    static void logInfo(int responseCode, HttpRequestParser requestParser) {
        if (MockServer.loggingEnabled) {
            System.out.printf(logString, Logger.ConsoleColors.BLUE, getLog(responseCode, requestParser), Logger.ConsoleColors.RESET);
        }
    }

    static void logWarning(int responseCode, HttpRequestParser requestParser) {
        if (MockServer.loggingEnabled) {
            System.out.printf(logString, Logger.ConsoleColors.YELLOW, getLog(responseCode, requestParser), Logger.ConsoleColors.RESET);
        }
    }

    private static String getLog(int responseCode, HttpRequestParser requestParser) {
        return String.format("%s %s: %d %s",
                requestParser.getMethod(),
                requestParser.getPath(),
                responseCode, EverestResponse.getReasonPhrase(responseCode));
    }
}