package com.google.accessmanager;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.GetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.model.Policy;
import com.google.api.services.cloudresourcemanager.model.SetIamPolicyRequest;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import java.io.IOException;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class GoogleProjectRolesManagerTest {
  private static final VerificationMode IS_INVOKED_ONCE = times(1);

  @Rule
  //CSOFF: VisibilityModifier
  public ExpectedException expectedException = ExpectedException.none();
  //CSON: VisibilityModifier

  @Mock
  private GoogleAdminSettings projectSettings;
  @Mock
  private CloudResourceManager cloudResourceManager;
  @Mock
  private GoogleCloudConnections googleCloudConnections;
  @Mock
  private CloudResourceManager.Projects projects;

  private CloudResourceManager.Projects.GetIamPolicy getIamPolicy;
  private CloudResourceManager.Projects.SetIamPolicy setIamPolicy;
  private GoogleProjectRolesManager projectRolesManager;

  @Before
  public void setUp() throws Exception {
    projectRolesManager = new GoogleProjectRolesManager(projectSettings);
    when(projectSettings.getProjectId()).thenReturn("someProject");

    projectRolesManager.setGoogleCloudConnections(googleCloudConnections);

    givenCloudResourceManager();
  }

  private void givenCloudResourceManager() {
    when(googleCloudConnections.getCloudResourceManager(any(GoogleAdminSettings.class))).thenReturn(
        cloudResourceManager);

    when(cloudResourceManager.projects()).thenReturn(projects);
  }

  @Test
  public void grantRole_givenRoleDoesntExists_thenCreatesRoleAndGrantsRole() throws IOException {
    givenPolicyWithServiceAccountAndRole(null, null);
    givenSetIamPolicyRequest();

    projectRolesManager.grantRole("some-service-account", Role.EDITOR);

    verify(projects, IS_INVOKED_ONCE).setIamPolicy(
        eq("someProject"), argThat(
            PolicyMatcher.hasRoleForServiceAccount(
                "some-service-account", Role.EDITOR)));
    verify(
        setIamPolicy, IS_INVOKED_ONCE).execute();
  }

  private void givenPolicyWithServiceAccountAndRole(String serviceAccount, Role role) throws IOException {
    getIamPolicy = mock(CloudResourceManager.Projects.GetIamPolicy.class);
    when(projects.getIamPolicy(anyString(), any(GetIamPolicyRequest.class))).thenReturn(getIamPolicy);

    Policy policy = new Policy();
    if (role != null) {
      Binding binding = new Binding();
      binding.setRole(role.getRoleName());
      if (serviceAccount != null) {
        binding.setMembers(Collections.singletonList("serviceAccount:" + serviceAccount));
      }
      policy.setBindings(Collections.singletonList(binding));
    }

    when(getIamPolicy.execute()).thenReturn(policy);
  }

  private void givenSetIamPolicyRequest() throws IOException {
    setIamPolicy = mock(CloudResourceManager.Projects.SetIamPolicy.class);
    when(projects.setIamPolicy(anyString(), any(SetIamPolicyRequest.class))).thenReturn(setIamPolicy);

    when(setIamPolicy.execute()).thenReturn(new Policy());
  }

  @Test
  public void grantRole_givenRoleAndServiceAccount_thenGrantsRoleToServiceAccount() throws IOException {
    givenPolicyWithServiceAccountAndRole(null, Role.EDITOR);
    givenSetIamPolicyRequest();

    projectRolesManager.grantRole("some-service-account", Role.EDITOR);

    verify(projects, IS_INVOKED_ONCE).setIamPolicy(
        eq("someProject"), argThat(
            PolicyMatcher.hasRoleForServiceAccount(
                "some-service-account", Role.EDITOR)));
    verify(setIamPolicy, IS_INVOKED_ONCE).execute();
  }

  @Test
  public void grantRole_givenGrantRoleFails_thenThrowsGoogleRoleException() throws IOException {
    expectedException.expect(GoogleRoleException.class);
    expectedException.expectMessage(
        "Unable to set role [roles/editor] for service account [some-service-account]");

    givenPolicyWithServiceAccountAndRole(null, Role.EDITOR);
    givenSetIamPolicyRequest();
    doThrow(IOException.class).when(setIamPolicy).execute();

    projectRolesManager.grantRole("some-service-account", Role.EDITOR);
  }

  @Test
  public void getIamPolicy_givenGetPolicyFails_thenThrowsGoogleRoleException() throws IOException {
    expectedException.expect(GoogleRoleException.class);
    expectedException.expectMessage("Unable to fetch access policies");

    givenPolicyWithServiceAccountAndRole(null, null);
    doThrow(IOException.class).when(getIamPolicy).execute();

    projectRolesManager.getIamPolicy();
  }

  @Test
  public void deleteRoleIfExists_givenRoleAndServiceAccount_thenGrantsRoleToServiceAccount() throws IOException {
    givenPolicyWithServiceAccountAndRole("some-service-account", Role.EDITOR);
    givenSetIamPolicyRequest();

    projectRolesManager.deleteRoleIfExists("some-service-account", Role.EDITOR);

    verify(projects, IS_INVOKED_ONCE).setIamPolicy(
        eq("someProject"), argThat(
            not(
                PolicyMatcher.hasRoleForServiceAccount(
                    "some-service-account", Role.EDITOR))));
    verify(setIamPolicy, IS_INVOKED_ONCE).execute();
  }

  @Test
  public void deleteRoleIfExists_givenGrantRoleFails_thenThrowsGoogleRoleException() throws IOException {
    expectedException.expect(GoogleRoleException.class);
    expectedException.expectMessage(
        "Unable to delete role [roles/editor] for service account [some-service-account]");

    givenPolicyWithServiceAccountAndRole("some-service-account", Role.EDITOR);
    givenSetIamPolicyRequest();
    doThrow(IOException.class).when(setIamPolicy).execute();

    projectRolesManager.deleteRoleIfExists("some-service-account", Role.EDITOR);
  }

  @Test
  public void hasRole_givenRoleExistsForServiceAccount_thenReturnTrue() throws IOException {
    givenPolicyWithServiceAccountAndRole("some-service-account", Role.EDITOR);

    boolean hasRole = projectRolesManager.hasRole("some-service-account", Role.EDITOR);

    assertThat(hasRole, is(true));
  }

  @Test
  public void hasRole_givenRoleDoesNotExistsForServiceAccount_thenReturnTrue() throws IOException {
    givenPolicyWithServiceAccountAndRole(null, Role.EDITOR);

    boolean hasRole = projectRolesManager.hasRole("some-service-account", Role.EDITOR);

    assertThat(hasRole, is(false));
  }

  private static class PolicyMatcher {
    static Matcher<SetIamPolicyRequest> hasRoleForServiceAccount(
        final String serviceAccount, final Role role) {
      return new BaseMatcher<SetIamPolicyRequest>() {
        @Override
        public boolean matches(Object item) {
          Policy policy = ((SetIamPolicyRequest) item).getPolicy();
          if (policy.getBindings() == null) {
            return false;
          }
          for (Binding binding : policy.getBindings()) {
            if (StringUtils.equals(binding.getRole(), role.getRoleName())) {
              if (binding.getMembers().contains("serviceAccount:" + serviceAccount)) {
                return true;
              }
            }
          }
          return false;
        }

        @Override
        public void describeTo(Description description) {
          description.appendText(
              format(
                  "Policy with role %s for service account %s", role.getRoleName(), serviceAccount));
        }
      };
    }
  }
}
