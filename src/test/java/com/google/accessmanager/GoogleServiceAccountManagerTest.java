package com.google.accessmanager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.Empty;
import com.google.api.services.iam.v1.model.ServiceAccount;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class GoogleServiceAccountManagerTest {
  private static final VerificationMode IS_INVOKED_ONCE = times(1);

  @Rule
  //CSOFF: VisibilityModifier
  public ExpectedException expectedException = ExpectedException.none();
  //CSON: VisibilityModifier

  @Mock
  private GoogleAdminSettings projectSettings;
  @Mock
  private Iam iam;
  @Mock
  private GoogleCloudConnections googleCloudConnections;
  @Mock
  private Iam.Projects projects;

  private Iam.Projects.ServiceAccounts.Create create;
  private Iam.Projects.ServiceAccounts.Delete delete;
  private GoogleServiceAccountManager serviceAccountManager;

  @Before
  public void setUp() throws Exception {
    serviceAccountManager = new GoogleServiceAccountManager(projectSettings);
    when(projectSettings.getProjectId()).thenReturn("someProject");

    serviceAccountManager.setGoogleCloudConnections(googleCloudConnections);

    givenCloudResourceManager();
  }

  private void givenCloudResourceManager() {
    when(googleCloudConnections.getAccessManager(any(GoogleAdminSettings.class))).thenReturn(
        iam);

    when(iam.projects()).thenReturn(projects);
  }

  @Test
  public void createServiceAccount_givenProjectSettings_thenCreateServiceAccount() throws IOException {
    givenServiceAccountsApi();

    ServiceAccount serviceAccount = serviceAccountManager.createServiceAccount("service-account-name");

    verify(create, IS_INVOKED_ONCE).execute();
    assertThat(serviceAccount, is(notNullValue()));
  }

  private void givenServiceAccountsApi() throws IOException {
    Iam.Projects.ServiceAccounts serviceAccounts = mock(Iam.Projects.ServiceAccounts.class);
    when(projects.serviceAccounts()).thenReturn(serviceAccounts);

    create = mock(Iam.Projects.ServiceAccounts.Create.class);
    when(serviceAccounts.create(anyString(), any(CreateServiceAccountRequest.class))).thenReturn(create);
    when(create.execute()).thenReturn(new ServiceAccount());

    delete = mock(Iam.Projects.ServiceAccounts.Delete.class);
    when(serviceAccounts.delete(anyString())).thenReturn(delete);
    when(delete.execute()).thenReturn(new Empty());
  }

  @Test
  public void createServiceAccount_givenCreateAccountThrowsException_thenThrowsGoogleAccessException()
      throws IOException {
    expectedException.expect(GoogleAccessException.class);
    expectedException.expectMessage(
        "Unable to create service account with display name [service-account-name]");

    givenServiceAccountsApi();
    doThrow(IOException.class).when(create).execute();

    serviceAccountManager.createServiceAccount("service-account-name");
  }

  @Test
  public void deleteServiceAccount_givenProjectSettings_thenDeleteServiceAccount() throws IOException {
    givenServiceAccountsApi();
    ServiceAccount serviceAccount = givenServiceAccount();

    serviceAccountManager.deleteServiceAccount(serviceAccount);

    verify(delete, IS_INVOKED_ONCE).execute();
  }

  private ServiceAccount givenServiceAccount() {
    ServiceAccount serviceAccount = new ServiceAccount();
    serviceAccount.setName("service-account-name");
    return serviceAccount;
  }

  @Test
  public void createServiceAccount_givenDeleteAccountThrowsException_thenThrowsGoogleAccessException()
      throws IOException {
    expectedException.expect(GoogleAccessException.class);
    expectedException.expectMessage(
        "Unable to delete service account [{name=service-account-name}]");

    givenServiceAccountsApi();
    ServiceAccount serviceAccount = givenServiceAccount();
    doThrow(IOException.class).when(delete).execute();

    serviceAccountManager.deleteServiceAccount(serviceAccount);
  }
}