package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Provoque une réponse 400 Bad Request
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }
}