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

package com.rohitawate.everest.format;

import java.io.IOException;

/**
 * Formats strings in various data formats.
 */
public interface Formatter {
    /**
     * Returns a string formatted appropriate to the data format.
     *
     * @param unformatted The unformatted string
     * @throws IOException If the Formatter fails to format the given string.
     */
    String format(String unformatted) throws IOException;
}