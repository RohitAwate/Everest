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

package com.rohitawate.everest.notifications;

import com.jfoenix.controls.JFXSnackbar;

import java.util.ArrayList;

public class NotificationsManager {
    private static ArrayList<JFXSnackbar> snackbars = new ArrayList<>();

    public static void registerChannel(JFXSnackbar snackbar) {
        snackbars.add(snackbar);
    }

    public static void push(String message, long duration) {
        for (JFXSnackbar snackbar : snackbars)
            snackbar.show(message, duration);
    }
}
