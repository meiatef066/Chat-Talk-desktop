package com.example.backend_chat.exception;

public class FriendRequestAlreadyExist extends RuntimeException {
  public FriendRequestAlreadyExist( String message) {
    super(message);
  }
}
