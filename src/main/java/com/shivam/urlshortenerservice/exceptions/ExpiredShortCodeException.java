package com.shivam.urlshortenerservice.exceptions;

public class ExpiredShortCodeException extends RuntimeException {
    public ExpiredShortCodeException(String message) {
        super(message);
    }
}
