package com.google.cloudsql.instance.model;

public class CloudSqlInstance {

  private final String name;
  private final String serviceAccountEmailAddress;
  private final CloudSqlInstanceSettings cloudSqlInstanceSettings;


  public CloudSqlInstance(String name, String serviceAccountEmailAddress, CloudSqlInstanceSettings settings) {
    this.name = name;
    this.serviceAccountEmailAddress = serviceAccountEmailAddress;
    this.cloudSqlInstanceSettings = settings;
  }

  public String getName() {
    return name;
  }

  public String getServiceAccountEmailAddress() {
    return serviceAccountEmailAddress;
  }

  public CloudSqlInstanceSettings getCloudSqlInstanceSettings() {
    return cloudSqlInstanceSettings;
  }

  @Override
  public String toString() {
    return "CloudSqlInstance{"
        + "name='"
        + name
        + '\''
        + ", serviceAccountEmailAddress='"
        + serviceAccountEmailAddress
        + '\''
        + ", cloudSqlInstanceSettings="
        + cloudSqlInstanceSettings
        + '}';
  }
}
