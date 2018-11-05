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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;

public class LoggingService {
    private static ExecutorService executor = EverestUtilities.newDaemonSingleThreadExecutor();
    private static final Logger logger = new Logger(Level.INFO);
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final Log log = new Log();

    public static void logSevere(String message, Exception exception, LocalDateTime time) {
        log(message, exception, time, Level.SEVERE);
    }

    public static void logWarning(String message, Exception exception, LocalDateTime time) {
        log(message, exception, time, Level.WARNING);
    }

    public static void logInfo(String message, LocalDateTime time) {
        log(message, null, time, Level.INFO);
    }

    private static void log(String message, Exception exception, LocalDateTime time, Level level) {
        log.message = message;
        log.exception = exception;
        log.time = dateFormat.format(time);
        log.level = level;
        executor.execute(logThread);
    }

    private static Runnable logThread = () -> logger.log(log);
}
