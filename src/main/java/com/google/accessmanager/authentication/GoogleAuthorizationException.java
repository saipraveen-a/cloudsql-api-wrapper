package com.google.accessmanager.authentication;

public class GoogleAuthorizationException extends RuntimeException {
  public GoogleAuthorizationException(String message, Throwable cause) {
    super(message, cause);
  }
}
