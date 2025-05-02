package com.shivam.urlshortenerservice.advices;

import com.shivam.urlshortenerservice.dtos.ExceptionDto;
import com.shivam.urlshortenerservice.exceptions.ExpiredShortCodeException;
import com.shivam.urlshortenerservice.exceptions.InvalidDateFormatException;
import com.shivam.urlshortenerservice.exceptions.ShortCodeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<ExceptionDto> handleInvalidDateFormat(InvalidDateFormatException ex) {
        return new ResponseEntity<>(new ExceptionDto(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ShortCodeNotFoundException.class)
    public ResponseEntity<ExceptionDto> handleIllegalArgument(ShortCodeNotFoundException ex) {
        return new ResponseEntity<>(new ExceptionDto(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExpiredShortCodeException.class)
    public ResponseEntity<ExceptionDto> handleIllegalState(ExpiredShortCodeException ex) {
        return new ResponseEntity<>(new ExceptionDto(ex.getMessage()), HttpStatus.GONE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDto> handleGeneric(Exception ex) {
        return new ResponseEntity<>(new ExceptionDto(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
