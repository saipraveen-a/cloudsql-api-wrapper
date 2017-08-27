package com.google.cloudsql.instance.service;

import com.google.accessmanager.authentication.GoogleCredentialBuilder;
import com.google.cloudsql.google.GoogleCloudSqlConfiguration;
import com.google.cloudsql.instance.model.CloudSqlInstanceSettings;
import com.google.cloudsql.instance.operation.OperationStatus;
import com.google.cloudsql.instance.operation.OperationStatusChecker;
import com.google.cloudsql.instance.operation.OperationStatusRetryerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.model.AclEntry;
import com.google.api.services.sqladmin.model.DatabaseInstance;
import com.google.api.services.sqladmin.model.IpConfiguration;
import com.google.api.services.sqladmin.model.Operation;
import com.google.api.services.sqladmin.model.Settings;
import com.google.api.services.sqladmin.model.User;

import java.util.Collections;

final class GoogleCloudSqlAdminHelper {

  private static final String AUTHORIZED_NETWORK_NAME = "Jenkins";
  private static SQLAdmin sqlAdminClient;

  private GoogleCloudSqlAdminHelper() {}

  static SQLAdmin getSqlAdmin(GoogleCloudSqlConfiguration config) {
    if (sqlAdminClient == null) {
      HttpTransport transport = new NetHttpTransport();
      JsonFactory jsonFactory = new JacksonFactory();
      Credential credential = GoogleCredentialBuilder
          .getCredentials(config.getAdminServiceAccountKeyFile(), config.getAdminServiceAccount());
      sqlAdminClient = new SQLAdmin.Builder(transport, jsonFactory, credential)
          .setServicePath("sql/v1beta4/")
          .setApplicationName(config.getProjectId())
          .build();
    }
    return sqlAdminClient;
  }

  static DatabaseInstance getDatabaseInstanceWithSettings(
      String instanceName,
      String projectId,
      CloudSqlInstanceSettings settings) {
    DatabaseInstance content = new DatabaseInstance();
    content.setProject(projectId);
    content.setName(instanceName);

    Settings googleSettings = getInstanceSettings(settings);
    IpConfiguration ipConfiguration = getInstanceIpConfiguration(settings);
    googleSettings.setIpConfiguration(ipConfiguration);

    content.setSettings(googleSettings);

    return content;
  }

  private static IpConfiguration getInstanceIpConfiguration(CloudSqlInstanceSettings settings) {
    IpConfiguration ipConfiguration = new IpConfiguration();
    AclEntry entry = new AclEntry();
    entry.setName(AUTHORIZED_NETWORK_NAME);
    entry.setValue(settings.getAuthorizedNetwork());
    ipConfiguration.setAuthorizedNetworks(Collections.singletonList(entry));
    ipConfiguration.setIpv4Enabled(true);
    return ipConfiguration;
  }

  private static Settings getInstanceSettings(CloudSqlInstanceSettings settings) {
    Settings googleSettings = new Settings();
    googleSettings.setActivationPolicy(settings.getActivationPolicy().getName());
    googleSettings.setTier(settings.getTier().getName());
    googleSettings.setPricingPlan(settings.getPricingPlan().getName());
    return googleSettings;
  }

  static OperationStatus waitUntilOperationIsDone(String projectId, Operation operation) {
    OperationStatusChecker statusChecker = new OperationStatusChecker(
        sqlAdminClient,
        projectId,
        operation.getName(),
        new OperationStatusRetryerFactory<OperationStatus>());
    return statusChecker.checkStatus();
  }

  static User getUserContent(String user, String password) {
    User userContent = new User();
    userContent.setName(user);
    userContent.setPassword(password);
    userContent.setHost("%");
    return userContent;
  }

  static void setSqlAdminClient(SQLAdmin sqlAdmin) {
    sqlAdminClient = sqlAdmin;
  }
}
