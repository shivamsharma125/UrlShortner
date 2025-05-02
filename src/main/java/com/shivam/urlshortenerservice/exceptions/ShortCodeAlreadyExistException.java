package com.shivam.urlshortenerservice.exceptions;

public class ShortCodeAlreadyExistException extends RuntimeException {
    public ShortCodeAlreadyExistException(String message) {
        super(message);
    }
}
