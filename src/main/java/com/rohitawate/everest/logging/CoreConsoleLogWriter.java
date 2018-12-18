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

public class CoreConsoleLogWriter implements LogWriter {
    private final Level level;

    CoreConsoleLogWriter(Level level) {
        this.level = level;
    }

    @Override
    public synchronized void append(Log log) {
        CoreLog coreLog = (CoreLog) log;
        if (coreLog.level.greaterThanEqualTo(this.level)) {
            if (coreLog.level.equals(Level.INFO)) {
                System.out.println(ConsoleColors.BLUE + coreLog.level + " " + DATE_FORMAT.format(coreLog.time) + ": " + coreLog.message + ConsoleColors.RESET);
            } else if (coreLog.level.equals(Level.SEVERE)) {
                System.out.println(ConsoleColors.RED + coreLog.level + " " + DATE_FORMAT.format(coreLog.time) + ": " + coreLog.message + ConsoleColors.RESET);
            } else if (coreLog.level.equals(Level.WARNING)) {
                System.out.println(ConsoleColors.YELLOW + coreLog.level + " " + DATE_FORMAT.format(coreLog.time) + ": " + coreLog.message + ConsoleColors.RESET);
            }
        }
    }
}
