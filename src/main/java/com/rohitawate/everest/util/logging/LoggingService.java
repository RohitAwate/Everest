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

package com.rohitawate.everest.util.logging;

import com.rohitawate.everest.util.Services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingService {
    private Logger logger;
    private DateTimeFormatter dateFormat;
    private String message;
    private Exception exception;
    private LocalDateTime time;

    private SevereLogger severeLogger = new SevereLogger();
    private WarningLogger warningLogger = new WarningLogger();
    private InfoLogger infoLogger = new InfoLogger();

    public LoggingService(Level writerLevel) {
        logger = new Logger(writerLevel);
        dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    }

    public void logSevere(String message, Exception exception, LocalDateTime time) {
        setValues(message, exception, time);
        Services.singleExecutor.execute(severeLogger);
    }

    public void logWarning(String message, Exception exception, LocalDateTime time) {
        setValues(message, exception, time);
        Services.singleExecutor.execute(warningLogger);
    }

    public void logInfo(String message, LocalDateTime time) {
        setValues(message, null, time);
        Services.singleExecutor.execute(infoLogger);
    }

    private void setValues(String message, Exception exception, LocalDateTime time) {
        this.message = message;
        this.exception = exception;
        this.time = time;
    }

    private class SevereLogger implements Runnable {
        @Override
        public void run() {
            System.out.println(message);
            Log log = new Log();
            log.setLevel(Level.SEVERE);
            log.setMessage(message);
            log.setException(exception);
            log.setTime(dateFormat.format(time));
            logger.log(log);
        }
    }

    private class WarningLogger implements Runnable {
        @Override
        public void run() {
            System.out.println(message);
            Log log = new Log();
            log.setLevel(Level.WARNING);
            log.setMessage(message);
            log.setException(exception);
            log.setTime(dateFormat.format(time));
            logger.log(log);
        }
    }


    private class InfoLogger implements Runnable {
        @Override
        public void run() {
            System.out.println(message);
            Log log = new Log();
            log.setLevel(Level.INFO);
            log.setMessage(message);
            log.setTime(dateFormat.format(time));
            logger.log(log);
        }
    }
}
