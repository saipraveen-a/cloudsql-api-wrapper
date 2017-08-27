package com.google.cloudsql.instance.operation;


public enum OperationStatus {
  DONE("DONE");

  private String displayName;

  OperationStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
