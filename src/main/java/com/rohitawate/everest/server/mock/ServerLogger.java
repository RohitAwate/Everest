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

import com.rohitawate.everest.http.HttpRequest;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.models.responses.EverestResponse;

class ServerLogger {
    private static final String logString = "%s[MockServer: %s] %s%s\n";

    static void logInfo(String serviceName, int responseCode, HttpRequest request) {
        System.out.printf(logString, Logger.ConsoleColors.BLUE, serviceName, getLog(responseCode, request), Logger.ConsoleColors.RESET);
    }

    static void logWarning(String serviceName, int responseCode, HttpRequest request) {
        System.out.printf(logString, Logger.ConsoleColors.YELLOW, serviceName, getLog(responseCode, request), Logger.ConsoleColors.RESET);
    }

    private static String getLog(int responseCode, HttpRequest request) {
        return String.format("%s %s %d %s",
                request.getMethod(),
                request.getPath(),
                responseCode, EverestResponse.getReasonPhrase(responseCode));
    }
}