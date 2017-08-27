package com.google.accessmanager;

import static java.lang.String.format;

import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.Empty;
import com.google.api.services.iam.v1.model.ServiceAccount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class GoogleServiceAccountManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleServiceAccountManager.class);

  private final GoogleAdminSettings googleAdminSettings;
  private GoogleCloudConnections googleCloudConnections;

  public GoogleServiceAccountManager(GoogleAdminSettings googleAdminSettings) {
    this.googleAdminSettings = googleAdminSettings;
    this.googleCloudConnections = new GoogleCloudConnections();
  }

  private Iam iam() {
    return googleCloudConnections.getAccessManager(googleAdminSettings);
  }

  ServiceAccount createServiceAccount(String serviceAccountDisplayName) {
    LOGGER.info("Create service account {}", serviceAccountDisplayName);
    try {
      CreateServiceAccountRequest createServiceAccountRequest = new CreateServiceAccountRequest();
      createServiceAccountRequest.setAccountId(serviceAccountDisplayName);

      ServiceAccount serviceAccount = new ServiceAccount();
      serviceAccount.setDisplayName(serviceAccountDisplayName);
      createServiceAccountRequest.setServiceAccount(serviceAccount);

      Iam.Projects.ServiceAccounts.Create create = iam().projects().serviceAccounts().create(
          "projects/" + googleAdminSettings.getProjectId(), createServiceAccountRequest);
      return create.execute();
    } catch (IOException e) {
      throw new GoogleAccessException(
          format(
              "Unable to create service account with display name [%s]", serviceAccountDisplayName),
          e);
    }
  }

  boolean deleteServiceAccount(ServiceAccount serviceAccount) {
    LOGGER.info("Delete service account {}", serviceAccount.getEmail());
    try {
      Iam.Projects.ServiceAccounts.Delete delete = iam().projects().serviceAccounts().delete(
          "projects/" + googleAdminSettings.getProjectId() + "/serviceAccounts/" + serviceAccount.getEmail());
      Empty empty = delete.execute();
      return empty.isEmpty();
    } catch (IOException e) {
      throw new GoogleAccessException(format("Unable to delete service account [%s]", serviceAccount), e);
    }
  }

  void setGoogleCloudConnections(GoogleCloudConnections googleCloudConnections) {
    this.googleCloudConnections = googleCloudConnections;
  }
}
