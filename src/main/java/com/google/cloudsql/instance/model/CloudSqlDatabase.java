package com.google.cloudsql.instance.model;

public class CloudSqlDatabase {
  private String name;

  public CloudSqlDatabase(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
