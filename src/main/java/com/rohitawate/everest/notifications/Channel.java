package com.rohitawate.everest.notifications;

public interface Channel {
    /**
     * Displays the given message for the specified duration using the notification channel.
     */
    void push(String message, long duration);
}
