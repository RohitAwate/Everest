package com.rohitawate.everest.exceptions;

public class DuplicateException extends Exception {
    public DuplicateException(String entity, String identifier) {
        super("Duplicate " + entity + ": " + identifier + " already exists.");
    }
}
