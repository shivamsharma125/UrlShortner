package com.shivam.urlshortenerservice.exceptions;

public class InvalidDateFormatException extends RuntimeException {
    public InvalidDateFormatException(String message) {
        super(message);
    }
}

