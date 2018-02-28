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

package com.rohitawate.restaurant.util.logging;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

class Logger {
    private Level writerLevel;
    private String logEntryTemplate;
    private String logFilePath = "RESTaurant/logs/" + LocalDate.now() + ".html";

    Logger(Level writerLevel) {
        this.writerLevel = writerLevel;

        createLogsFile();
        logEntryTemplate = readFile(getClass().getResourceAsStream("/templates/LogEntry.html"));
    }

    /**
     * Appends the log to the respective day's log file.
     *
     * @param log - The log to be written to file.
     */
    void log(Log log) {
        if (log.getLevel().greaterThanEqualTo(this.writerLevel)) {
            try {
                String logFileContents = readFile(logFilePath);
                String logEntry = generateLogEntry(log);
                logFileContents = logFileContents.replace("<!-- Placeholder for new log -->", logEntry);
                BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath));
                writer.flush();
                writer.write(logFileContents);
                writer.close();
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
    private String generateLogEntry(Log log) {
        String logEntry = this.logEntryTemplate;
        logEntry = logEntry.replace("%% LogLevel %%", log.getLevel().toString());
        logEntry = logEntry.replace("%% Time %%", log.getTime());
        logEntry = logEntry.replace("%% Message %%", log.getMessage());
        StringBuilder builder = new StringBuilder();

        if (log.getException() != null) {
            StackTraceElement[] stackTrace = log.getException().getStackTrace();
            if (stackTrace.length != 0) {
                for (StackTraceElement element : log.getException().getStackTrace()) {
                    builder.append(" -- ");
                    builder.append(element.toString());
                    builder.append("<br>\n");
                }
            } else {
                builder.append("N/A");
            }
        } else {
            logEntry = logEntry.replace("Stack Trace: <br>", "");
            builder.append("");
        }

        logEntry = logEntry.replace("%% StackTrace %%", builder.toString());

        return logEntry;
    }

    private void createLogsFile() {
        File logsDirectory = new File("RESTaurant/logs/");
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

    private String readFile(String filePath) {
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            Scanner scanner = new Scanner(bufferedReader);

            while (scanner.hasNext()) {
                builder.append(scanner.nextLine());
                builder.append("\n");
            }
            scanner.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }
}
