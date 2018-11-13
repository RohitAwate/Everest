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

import com.rohitawate.everest.models.responses.EverestResponse;

public class ServerConsoleLogWriter implements LogWriter {
    private final Level level;

    ServerConsoleLogWriter(Level level) {
        this.level = level;
    }

    @Override
    public void append(Log log) {
        ServerLog serverLog = (ServerLog) log;
        if (serverLog.level.greaterThanEqualTo(this.level)) {
            String msg = String.format("%s [MockServer] [%s] %s %s %d %s",
                    DATE_FORMAT.format(serverLog.time),
                    serverLog.serverName,
                    serverLog.request.getMethod(),
                    serverLog.request.getPath(),
                    serverLog.responseCode,
                    EverestResponse.getReasonPhrase(serverLog.responseCode));
            if (serverLog.level.equals(Level.INFO)) {
                System.out.println(ConsoleColors.BLUE + msg + ConsoleColors.RESET);
            } else if (serverLog.level.equals(Level.SEVERE)) {
                System.out.println(ConsoleColors.RED + msg + ConsoleColors.RESET);
            } else if (serverLog.level.equals(Level.WARNING)) {
                System.out.println(ConsoleColors.YELLOW + msg + ConsoleColors.RESET);
            }
        }
    }
}
