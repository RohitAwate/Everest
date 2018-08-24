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

import com.google.common.util.concurrent.MoreExecutors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;

public class LoggingService {
    private static Executor executor = MoreExecutors.directExecutor();
    private static final Logger logger = new Logger(Level.INFO);
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    static final Log log = new Log();

    public static void logSevere(String message, Exception exception, LocalDateTime time) {
        setValues(message, exception, time);
        executor.execute(severeLogger);
    }

    public static void logWarning(String message, Exception exception, LocalDateTime time) {
        setValues(message, exception, time);
        executor.execute(warningLogger);
    }

    public static void logInfo(String message, LocalDateTime time) {
        setValues(message, null, time);
        executor.execute(infoLogger);
    }

    private static void setValues(String message, Exception exception, LocalDateTime time) {
        log.message = message;
        log.exception = exception;
        log.time = dateFormat.format(time);
    }

    private static Runnable severeLogger = () -> {
        log.level = Level.SEVERE;
        logger.log();
    };

    private static Runnable warningLogger = () -> {
        log.level = Level.WARNING;
        logger.log();
    };

    private static Runnable infoLogger = () -> {
        log.level = Level.INFO;
        logger.log();
    };
}
