package com.google.accessmanager;

import static com.google.cloudsql.instance.CloudSqlInstanceTests.getTestProjectSettings;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.api.services.iam.v1.model.ServiceAccount;

import org.junit.Before;
import org.junit.Test;

public class GoogleProjectRoleManagerIntegrationTest {

  private static final int SERVICE_ACCOUNT_ID_LENGTH = 10;
  private GoogleProjectRolesManager googleProjectRolesManager;
  private GoogleServiceAccountManager googleServiceAccountManager;

  @Before
  public void setUp() {
    googleProjectRolesManager = new GoogleProjectRolesManager(getTestProjectSettings());
    googleServiceAccountManager = new GoogleServiceAccountManager(getTestProjectSettings());
  }

  @Test
  public void grantRole_givenAValidRoleWithServiceAccount_thenRoleIsGrantedToServiceAccount() {
    ServiceAccount serviceAccount = null;
    try {
      serviceAccount = googleServiceAccountManager
          .createServiceAccount("cloudsqlapitools-" + randomAlphabetic(SERVICE_ACCOUNT_ID_LENGTH).toLowerCase());
      String serviceAccountEmail = serviceAccount.getEmail();
      googleProjectRolesManager.deleteRoleIfExists(serviceAccountEmail, Role.EDITOR);
      assertThat(googleProjectRolesManager.hasRole(serviceAccountEmail, Role.EDITOR), is(false));

      googleProjectRolesManager.grantRole(serviceAccountEmail, Role.EDITOR);

      boolean roleAdded = googleProjectRolesManager.hasRole(serviceAccountEmail, Role.EDITOR);
      assertThat(roleAdded, is(true));
    } finally {
      if (serviceAccount != null) {
        googleProjectRolesManager.deleteRoleIfExists(serviceAccount.getEmail(), Role.EDITOR);
        googleServiceAccountManager.deleteServiceAccount(serviceAccount);
      }
    }
  }
}
