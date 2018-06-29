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

package com.rohitawate.everest.controllers;

import java.util.HashMap;

/**
 * Convenience class to abstract the state of the application.
 */
public class DashboardState {
    public String target;
    public String httpMethod;
    public HashMap<String, String> params;
    public HashMap<String, String> headers;
    public String contentType;
    public String rawBody;
    public String rawBodyType;
    public HashMap<String, String> urlStringTuples;
    public HashMap<String, String> formStringTuples;
    public HashMap<String, String> formFileTuples;
    public String binaryFilePath;
}
