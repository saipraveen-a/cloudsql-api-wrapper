package com.google.cloudsql.instance.model;

public class CloudSqlTable {

  private String tableName;

  public CloudSqlTable(String tableName) {
    this.tableName = tableName;
  }

  public String getName() {
    return tableName;
  }
}
