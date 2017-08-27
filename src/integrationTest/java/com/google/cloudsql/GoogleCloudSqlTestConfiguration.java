package com.google.cloudsql;

import com.google.cloudsql.google.GoogleCloudSqlConfiguration;

public class GoogleCloudSqlTestConfiguration implements GoogleCloudSqlConfiguration {

  private static final String PROJECT_ID = System.getProperty("appengineProject", "my-project");
  private static final String KEY_FILE =
      System.getProperty("appengineServiceAccountKeyFile", "/appengineServiceAccountKey.p12");
  private static final String SERVICE_ACCOUNT_EMAIL = System.getProperty(
      "appengineServiceAccountEmail",
      "someemail@developer.gserviceaccount.com");
  private static final String ROOT_PASSWORD = "";

  private final String instance;
  private final String database;

  public GoogleCloudSqlTestConfiguration() {
    this.instance = null;
    this.database = null;
  }

  public GoogleCloudSqlTestConfiguration(String instance) {
    this.instance = instance;
    this.database = null;
  }

  public GoogleCloudSqlTestConfiguration(String instance, String database) {
    this.instance = instance;
    this.database = database;
  }

  @Override
  public String getProjectId() {
    return PROJECT_ID;
  }

  @Override
  public String getInstance() {
    return instance;
  }

  @Override
  public String getInstanceRootPassword() {
    return ROOT_PASSWORD;
  }

  @Override
  public String getDatabase() {
    return database;
  }

  @Override
  public String getAdminServiceAccount() {
    return SERVICE_ACCOUNT_EMAIL;
  }

  @Override
  public String getAdminServiceAccountKeyFile() {
    return KEY_FILE;
  }
}
