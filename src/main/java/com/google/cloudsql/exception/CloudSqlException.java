package com.google.cloudsql.exception;

//CSOFF: MutableException
public class CloudSqlException extends RuntimeException {
  private String message;

  public CloudSqlException(String message) {
    super(message);
  }

  public CloudSqlException(String message, Throwable ex) {
    super(message, ex);
  }
}
