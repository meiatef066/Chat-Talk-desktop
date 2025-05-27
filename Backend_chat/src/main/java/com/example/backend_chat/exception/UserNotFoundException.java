package com.example.backend_chat.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException( String message ) {
        super(message);
    }
}
