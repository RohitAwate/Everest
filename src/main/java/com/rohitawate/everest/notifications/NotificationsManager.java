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
