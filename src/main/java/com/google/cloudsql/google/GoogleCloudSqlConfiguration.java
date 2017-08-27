package com.google.cloudsql.google;

public interface GoogleCloudSqlConfiguration {

  String getProjectId();

  String getInstance();

  String getInstanceRootPassword();

  String getDatabase();

  String getAdminServiceAccount();

  String getAdminServiceAccountKeyFile();
}
