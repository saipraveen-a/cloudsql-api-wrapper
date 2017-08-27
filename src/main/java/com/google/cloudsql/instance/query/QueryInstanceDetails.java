package com.google.cloudsql.instance.query;

public class QueryInstanceDetails {
  private final String projectId;
  private final String instanceName;
  private final String rootPassword;

  public QueryInstanceDetails(String projectId, String instanceName, String rootPassword) {
    this.projectId = projectId;
    this.instanceName = instanceName;
    this.rootPassword = rootPassword;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getInstanceName() {
    return instanceName;
  }

  String getRootPassword() {
    return rootPassword;
  }
}
