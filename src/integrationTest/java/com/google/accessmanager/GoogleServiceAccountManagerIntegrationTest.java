package com.google.accessmanager;

import static com.google.cloudsql.instance.CloudSqlInstanceTests.getTestProjectSettings;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.api.services.iam.v1.model.ServiceAccount;

import org.junit.Before;
import org.junit.Test;

public class GoogleServiceAccountManagerIntegrationTest {
  private static final int SERVICE_ACCOUNT_ID_LENGTH = 10;

  private GoogleServiceAccountManager googleServiceAccountManager;

  @Before
  public void setUp() throws Exception {
    googleServiceAccountManager = new GoogleServiceAccountManager(getTestProjectSettings());
  }

  @Test
  public void createServiceAccount_givenValidProjectAndServiceAccountName_thenCreatesNewServiceAccount() {
    ServiceAccount serviceAccount = googleServiceAccountManager.createServiceAccount(
        "cloudsqlapitools-" + randomAlphabetic(
            SERVICE_ACCOUNT_ID_LENGTH).toLowerCase());

    assertThat(serviceAccount, is(notNullValue()));

    boolean deleted = googleServiceAccountManager.deleteServiceAccount(serviceAccount);
    assertThat(deleted, is(true));
  }
}