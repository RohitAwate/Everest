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
    private Log log;

    public LoggingService(Level writerLevel) {
        this.log = new Log();
        this.logger = new Logger(writerLevel);
        this.dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
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
        this.log.message = message;
        this.log.exception = exception;
        this.log.time = dateFormat.format(time);
    }

    private Runnable severeLogger = () -> {
        this.log.level = Level.SEVERE;
        this.logger.log(this.log);
    };

    private Runnable warningLogger = () -> {
        this.log.level = Level.WARNING;
        this.logger.log(log);
    };

    private Runnable infoLogger = () -> {
        this.log.level = Level.INFO;
        this.logger.log(log);
    };
}
