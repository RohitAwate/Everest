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

package com.rohitawate.everest.logging;

import com.rohitawate.everest.models.responses.EverestResponse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

class ServerFileLogWriter implements LogWriter {
    private final Level level;

    private static final String LOGS_DIR_PATH = "Everest/logs/servers/";
    private static final String LOG_FILE_PATH = LOGS_DIR_PATH + LocalDate.now() + ".txt";
    private static BufferedWriter writer;

    ServerFileLogWriter(Level level) {
        this.level = level;

        try {
            File logsDir = new File(LOGS_DIR_PATH);
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            File logFile = new File(LOG_FILE_PATH);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true));
        } catch (IOException e) {
            System.err.println("Could not initialize ServerFileLogWriter");
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void append(Log log) {
        ServerLog serverLog = (ServerLog) log;
        if (serverLog.level.greaterThanEqualTo(this.level)) {
            try {
                writer.append(String.format(
                        "%s [%s] %s %s %d %s\n",
                        DATE_FORMAT.format(serverLog.time),
                        serverLog.serverName,
                        serverLog.request.getMethod(),
                        serverLog.request.getPath(),
                        serverLog.responseCode,
                        EverestResponse.getReasonPhrase(serverLog.responseCode)));
                writer.flush();
            } catch (IOException e) {
                System.err.println("Could not write append to file.");
                e.printStackTrace();
            }
        }
    }
}
