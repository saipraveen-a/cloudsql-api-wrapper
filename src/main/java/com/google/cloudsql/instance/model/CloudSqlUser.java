package com.google.cloudsql.instance.model;

public class CloudSqlUser {
  private String user;
  private String password;
  private String host;

  public CloudSqlUser(String user, String password, String host) {
    this.user = user;
    this.password = password;
    this.host = host;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getHost() {
    return host;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
