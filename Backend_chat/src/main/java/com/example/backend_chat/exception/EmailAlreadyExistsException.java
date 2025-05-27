package com.example.backend_chat.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException( String message ) {
        super(message);
    }
}
