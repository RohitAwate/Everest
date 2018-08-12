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

package com.rohitawate.everest.exceptions;

/**
 * Thrown when the server sends ambiguous responses.
 * <p>
 * Used by DashboardController to display ErrorLayer.
 */
public class NullResponseException extends Exception {
    private String exceptionTitle;
    private String exceptionDetails;

    public NullResponseException(String exceptionTitle, String exceptionDetails) {
        this.exceptionTitle = exceptionTitle;
        this.exceptionDetails = exceptionDetails;
    }

    public void setExceptionTitle(String exceptionTitle) {
        this.exceptionTitle = exceptionTitle;
    }

    public void setExceptionDetails(String exceptionDetails) {
        this.exceptionDetails = exceptionDetails;
    }

    public String getExceptionTitle() {
        return exceptionTitle;
    }

    public String getExceptionDetails() {
        return exceptionDetails;
    }
}
