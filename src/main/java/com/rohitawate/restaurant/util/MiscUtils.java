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

package com.rohitawate.restaurant.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class MiscUtils {
    /**
     * Removes leading and trailing quotation marks from strings.
     *
     * @param input String with leading and trailing quotation marks.
     * @return trimmedString - String with leading and trailing quotation marks removed.
     */
    public static String trimString(String input) {
        return input.replaceAll("\"", "");
    }

    /**
     * Copies the BugReporter from within the JAR to the installation directory.
     */
    public static void createBugReporter() {
        new Thread(() -> {
            File bugReporterFile = new File("RESTaurant/BugReporter.jar");
            if (!bugReporterFile.exists()) {
                InputStream inputStream = MiscUtils.class.getResourceAsStream("/BugReporter.jar");
                Path bugReporter = Paths.get("RESTaurant/BugReporter.jar");
                try {
                    Files.copy(inputStream, bugReporter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Services.loggingService.logInfo("BugReporter was copied to installation folder.", LocalDateTime.now());
            } else {
                Services.loggingService.logInfo("BugReporter was found.", LocalDateTime.now());
            }
        }).start();
    }
}
