package com.shivam.urlshortenerservice.exceptions;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message){
        super(message);
    }
}
