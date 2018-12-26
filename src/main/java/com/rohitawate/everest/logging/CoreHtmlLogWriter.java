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

import com.rohitawate.everest.misc.EverestUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class CoreHtmlLogWriter implements LogWriter {
    private final Level writerLevel;

    private static final String LOGS_DIR_PATH = "Everest/logs/core/";
    private static final String LOG_FILE_PATH = LOGS_DIR_PATH + LocalDate.now() + ".html";
    private static String LOG_ENTRY_TEMPLATE;
    private static BufferedWriter writer;

    static {
        try {
            LOG_ENTRY_TEMPLATE = EverestUtilities.readFile(CoreHtmlLogWriter.class.getResourceAsStream("/html/LogEntry.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    CoreHtmlLogWriter(Level writerLevel) {
        this.writerLevel = writerLevel;

        try {
            createLogsFile();
            writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true));
        } catch (IOException e) {
            System.err.println("Could not initialize CoreHtmlLogger.");
            e.printStackTrace();
        }
    }

    /**
     * Appends the append to the respective day's append file.
     */
    public synchronized void append(Log log) {
        CoreLog coreLog = (CoreLog) log;
        if (coreLog.level.greaterThanEqualTo(this.writerLevel)) {
            try {
                writer.append(getLogEntry(coreLog));
                writer.flush();
            } catch (IOException e) {
                System.err.println("Could not write append to file.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates HTML with the append information.
     * Different append levels are color-coded for improved readability.
     * <p>
     * Color codes:
     * Red = Severe
     * Yellow = Warning
     * Green = Info
     */
    private static String getLogEntry(CoreLog coreLog) {
        String logEntry = LOG_ENTRY_TEMPLATE;
        logEntry = logEntry.replace("%% LogLevel %%", coreLog.level.toString());
        logEntry = logEntry.replace("%% Time %%", DATE_FORMAT.format(coreLog.time));
        logEntry = logEntry.replace("%% Message %%", coreLog.message);
        StringBuilder builder = new StringBuilder();

        if (coreLog.exception != null) {
            StackTraceElement[] stackTrace = coreLog.exception.getStackTrace();
            builder.append(coreLog.exception.toString());
            builder.append("<br>\n");
            if (stackTrace.length != 0) {
                for (StackTraceElement element : coreLog.exception.getStackTrace()) {
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

    private static void createLogsFile() {
        File logsDirectory = new File(LOGS_DIR_PATH);
        if (!logsDirectory.exists())
            logsDirectory.mkdirs();

        File logsFile = new File(LOG_FILE_PATH);

        try {
            if (logsFile.exists())
                return;

            logsFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH));
            String logsFileTemplate = EverestUtilities.readFile(CoreHtmlLogWriter.class.getResourceAsStream("/html/LogsFile.html"));
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
}
