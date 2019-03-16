/*
 * Copyright 2019 Rohit Awate.
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

package com.rohitawate.everest.format;

import com.rohitawate.everest.exceptions.DuplicateException;
import com.rohitawate.everest.models.requests.HTTPConstants;

import java.util.HashMap;

/**
 * Provides Formatters for formatting strings of specific data formats.
 * <p>
 * Everest, by default, comes with a Formatter for JSON.
 */
public class FormatterFactory {
    private static HashMap<String, Formatter> formatters;

    static {
        formatters = new HashMap<>();
        formatters.put(HTTPConstants.JSON, new JSONFormatter());
    }

    public static Formatter getFormatter(String type) {
        return formatters.get(type);
    }


    /**
     * Provisional method for the future Extension API which will enable
     * developers to write and load custom Formatters.
     *
     * @param name      The display name for the Formatter.
     * @param formatter The Formatter object.
     * @throws DuplicateException If a Formatter is already loaded by Everest with the same name.
     */
    public static void addFormatter(String name, Formatter formatter)
            throws DuplicateException {
        if (formatters.containsKey(name)) {
            throw new DuplicateException("Formatter", name);
        }

        formatters.put(name, formatter);
    }
}
