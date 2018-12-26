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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.HashMap;

public class NotificationsManager {
    private static HashMap<String, Channel> snackbars = new HashMap<>();

    public static boolean registerChannel(String channelID, Channel channel) {
        if (snackbars.get(channelID) != null) {
            return false;
        }

        snackbars.put(channelID, channel);
        return true;
    }

    public static void push(String channelID, String message, long duration) {
        Channel channel = snackbars.get(channelID);

        if (channel != null) {
            channel.push(message, duration);
        }
    }

    // TODO: Implement this method
    public static void push(String channelID, String message, long duration, EventHandler<ActionEvent> handler) {

    }
}
