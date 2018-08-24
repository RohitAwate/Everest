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

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

class Logger {
    private Level writerLevel;
    private String logEntryTemplate;
    private String logFilePath = "Everest/logs/" + LocalDate.now() + ".html";

    Logger(Level writerLevel) {
        this.writerLevel = writerLevel;

        createLogsFile();
        logEntryTemplate = readFile(getClass().getResourceAsStream("/templates/LogEntry.html"));
    }

    /**
     * Appends the log to the respective day's log file.
     *
     */
    synchronized void log() {
        if (LoggingService.log.level.equals(Level.INFO)) {
            System.out.println(LoggingService.log.level + " " + LoggingService.log.time + ": " + LoggingService.log.message);
        } else {
            System.err.println(LoggingService.log.level + " " + LoggingService.log.time + ": " + LoggingService.log.message);
        }

        if (LoggingService.log.level.greaterThanEqualTo(this.writerLevel)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
                writer.flush();
                writer.append(getLogEntry(LoggingService.log));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates HTML with the log information.
     * Different log levels are color-coded for improved readability.
     * <p>
     * Color codes:
     * Red = Severe
     * Yellow = Warning
     * Green = Info
     */
    private String getLogEntry(Log log) {
        String logEntry = this.logEntryTemplate;
        logEntry = logEntry.replace("%% LogLevel %%", log.level.toString());
        logEntry = logEntry.replace("%% Time %%", log.time);
        logEntry = logEntry.replace("%% Message %%", log.message);
        StringBuilder builder = new StringBuilder();

        if (log.exception != null) {
            StackTraceElement[] stackTrace = log.exception.getStackTrace();
            builder.append(log.exception.toString());
            builder.append("<br>\n");
            if (stackTrace.length != 0) {
                for (StackTraceElement element : log.exception.getStackTrace()) {
                    builder.append(" -- ");
                    builder.append(element.toString());
                    builder.append("<br>\n");
                }
            } else {
                builder.append("Stack trace unavailable.");
            }
        }

        logEntry = logEntry.replace("%% StackTrace %%", builder.toString());

        return logEntry;
    }

    private void createLogsFile() {
        File logsDirectory = new File("Everest/logs/");
        if (!logsDirectory.exists())
            logsDirectory.mkdirs();

        File logsFile = new File(logFilePath);

        try {
            if (logsFile.exists())
                return;

            logsFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath));
            String logsFileTemplate = readFile(getClass().getResourceAsStream("/templates/LogsFile.html"));
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            logsFileTemplate = logsFileTemplate.replace("%% Date %%", dateTimeFormatter.format(LocalDate.now()));
            logsFileTemplate = logsFileTemplate.replace("%% Date %%", dateTimeFormatter.format(LocalDate.now()));
            writer.flush();
            writer.write(logsFileTemplate);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile(InputStream stream) {
        StringBuilder builder = new StringBuilder();
        Scanner scanner = new Scanner(stream);

        while (scanner.hasNext()) {
            builder.append(scanner.nextLine());
            builder.append("\n");
        }
        scanner.close();

        return builder.toString();
    }
}
