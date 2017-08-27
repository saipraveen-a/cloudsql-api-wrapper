package com.google.accessmanager;

import com.google.accessmanager.authentication.GoogleCredentialBuilder;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.iam.v1.Iam;

class GoogleCloudConnections {
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  CloudResourceManager getCloudResourceManager(GoogleAdminSettings googleAdminSettings) {
    String keyFile = googleAdminSettings.getKeyFile();
    String serviceAccount = googleAdminSettings.getServiceAccount();
    String projectId = googleAdminSettings.getProjectId();

    Credential credential = GoogleCredentialBuilder.getCredentials(keyFile, serviceAccount);
    return new CloudResourceManager.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(projectId).build();
  }

  Iam getAccessManager(GoogleAdminSettings googleAdminSettings) {
    String keyFile = googleAdminSettings.getKeyFile();
    String serviceAccount = googleAdminSettings.getServiceAccount();
    String projectId = googleAdminSettings.getProjectId();

    Credential credential = GoogleCredentialBuilder.getCredentials(keyFile, serviceAccount);
    return new Iam.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(projectId).build();
  }
}
