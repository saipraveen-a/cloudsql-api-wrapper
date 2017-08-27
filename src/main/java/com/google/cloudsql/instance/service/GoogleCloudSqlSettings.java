package com.google.cloudsql.instance.service;

import com.google.accessmanager.GoogleAdminSettings;

public class GoogleCloudSqlSettings implements GoogleAdminSettings {
  private String projectId;
  private String serviceAccount;
  private String keyFile;

  public GoogleCloudSqlSettings(String projectId, String serviceAccount, String keyFile) {
    this.projectId = projectId;
    this.serviceAccount = serviceAccount;
    this.keyFile = keyFile;
  }

  @Override
  public String getProjectId() {
    return projectId;
  }

  @Override
  public String getServiceAccount() {
    return serviceAccount;
  }

  @Override
  public String getKeyFile() {
    return keyFile;
  }
}
