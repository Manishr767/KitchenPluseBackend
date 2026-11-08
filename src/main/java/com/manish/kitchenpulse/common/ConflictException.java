package com.manish.kitchenpulse.common;

/** The request conflicts with existing data, e.g. a duplicate (HTTP 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
