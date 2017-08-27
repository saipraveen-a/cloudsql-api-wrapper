package com.google.accessmanager;

import static java.lang.String.format;

import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.GetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.model.Policy;
import com.google.api.services.cloudresourcemanager.model.SetIamPolicyRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GoogleProjectRolesManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleProjectRolesManager.class);

  private final GoogleAdminSettings googleAdminSettings;
  private GoogleCloudConnections googleCloudConnections;

  public GoogleProjectRolesManager(GoogleAdminSettings googleAdminSettings) {
    this.googleAdminSettings = googleAdminSettings;
    this.googleCloudConnections = new GoogleCloudConnections();
  }

  public void grantRole(String serviceAccountEmail, Role role) {
    try {
      LOGGER.info("Granting {} role to {}", role.getRoleName(), serviceAccountEmail);
      Policy policy = getIamPolicy();

      Policy updatedPolicy = grantRoleToServiceAccount(serviceAccountEmail, role, policy);

      SetIamPolicyRequest setIamPolicyRequest = new SetIamPolicyRequest();
      setIamPolicyRequest.setPolicy(updatedPolicy);

      CloudResourceManager.Projects.SetIamPolicy setIamPolicy = cloudResourceManager().projects().setIamPolicy(
          googleAdminSettings.getProjectId(), setIamPolicyRequest);

      setIamPolicy.execute();
    } catch (IOException ex) {
      throw new GoogleRoleException(
          format(
              "Unable to set role [%s] for service account [%s]", role.getRoleName(), serviceAccountEmail), ex);
    }
  }

  private CloudResourceManager cloudResourceManager() {
    return googleCloudConnections.getCloudResourceManager(googleAdminSettings);
  }

  Policy getIamPolicy() {
    CloudResourceManager.Projects.GetIamPolicy getIamPolicy;
    try {
      getIamPolicy = cloudResourceManager().projects().getIamPolicy(
          googleAdminSettings.getProjectId(), new GetIamPolicyRequest());

      return getIamPolicy.execute();
    } catch (IOException e) {
      throw new GoogleRoleException("Unable to fetch access policies", e);
    }
  }

  private Policy grantRoleToServiceAccount(String serviceAccountEmail, Role role, Policy policy) {
    checkAndCreateRole(role, policy);

    Binding binding = getRoleBinding(role, policy);
    String serviceAccountMember = serviceAccountMember(serviceAccountEmail);
    if (binding != null) {
      if (binding.getMembers() == null) {
        binding.setMembers(new ArrayList<String>());
      }
      if (!binding.getMembers().contains(serviceAccountMember)) {
        binding.getMembers().add(serviceAccountMember);
      }
    }
    return policy;
  }

  private void checkAndCreateRole(Role role, Policy policy) {
    List<Binding> bindings = policy.getBindings();
    if (bindings == null) {
      policy.setBindings(new ArrayList<Binding>());
    }
    Binding roleBinding = getRoleBinding(role, policy);
    if (roleBinding == null) {
      roleBinding = new Binding();
      roleBinding.setRole(role.getRoleName());
      roleBinding.setMembers(new ArrayList<String>());
      policy.getBindings().add(roleBinding);
    }
  }

  private Binding getRoleBinding(Role role, Policy policy) {
    if (policy.getBindings() != null) {
      for (Binding binding : policy.getBindings()) {
        if (StringUtils.equals(binding.getRole(), role.getRoleName())) {
          return binding;
        }
      }
    }
    return null;
  }

  private String serviceAccountMember(String serviceAccountEmail) {
    return "serviceAccount:" + serviceAccountEmail;
  }

  public void deleteRoleIfExists(String serviceAccountEmail, Role role) {
    LOGGER.info("Delete role {} for {}", role.getRoleName(), serviceAccountEmail);
    try {
      Policy policy = getIamPolicy();
      if (policy.getBindings() != null) {

        for (Binding binding : policy.getBindings()) {
          if (StringUtils.equals(binding.getRole(), role.getRoleName())) {
            removeServiceAccount(serviceAccountEmail, binding);
            break;
          }
        }
      }

      SetIamPolicyRequest setIamPolicyRequest = new SetIamPolicyRequest();
      setIamPolicyRequest.setPolicy(policy);

      CloudResourceManager.Projects.SetIamPolicy setIamPolicy = cloudResourceManager().projects().setIamPolicy(
          googleAdminSettings.getProjectId(), setIamPolicyRequest);

      setIamPolicy.execute();
    } catch (IOException ex) {
      throw new GoogleRoleException(
          format(
              "Unable to delete role [%s] for service account [%s]", role.getRoleName(), serviceAccountEmail), ex);
    }
  }

  private void removeServiceAccount(String serviceAccountEmail, Binding binding) {
    if (binding != null && binding.getMembers() != null) {
      List<String> members = binding.getMembers();
      List<String> updatedMembers = new ArrayList<>(members);
      updatedMembers.remove(serviceAccountMember(serviceAccountEmail));
      binding.setMembers(updatedMembers);
    }
  }

  public boolean hasRole(String serviceAccountEmail, Role role) {
    LOGGER.info("Check if service account {} has role {}", serviceAccountEmail, role.getRoleName());
    Policy policy = getIamPolicy();

    Binding binding = getRoleBinding(role, policy);
    if (binding != null && binding.getMembers() != null) {
      if (binding.getMembers().contains(serviceAccountMember(serviceAccountEmail))) {
        return true;
      }
    }
    return false;
  }

  void setGoogleCloudConnections(GoogleCloudConnections googleCloudConnections) {
    this.googleCloudConnections = googleCloudConnections;
  }
}
