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

/**
 * Holds default settings values which may
 * get overwritten by SettingsLoader.
 */
public class Settings {
    public static String responseAreaFont = "\"Liberation Mono\"";
    public static int responseAreaFontSize = 18;

    public static boolean connectionTimeOutEnable = false;
    public static int connectionTimeOut = 10000;

    public static boolean connectionReadTimeOutEnable = false;
    public static int connectionReadTimeOut = 30000;

    public static String theme = "Adreana";
}
