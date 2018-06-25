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

package com.rohitawate.everest.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

public class EverestUtilities {
    public static ObjectMapper jsonMapper;

    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

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
            InputStream inputStream = EverestUtilities.class.getResourceAsStream("/BugReporter.jar");
            Path bugReporter = Paths.get("Everest/BugReporter.jar");
            try {
                Files.copy(inputStream, bugReporter, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Services.loggingService.logInfo("BugReporter was copied to installation folder.", LocalDateTime.now());

        }).start();
    }
}
