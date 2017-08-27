package com.google.accessmanager;

public class GoogleRoleException extends RuntimeException {
  public GoogleRoleException(String message, Throwable cause) {
    super(message, cause);
  }

  public GoogleRoleException(String message) {
    super(message);
  }
}
