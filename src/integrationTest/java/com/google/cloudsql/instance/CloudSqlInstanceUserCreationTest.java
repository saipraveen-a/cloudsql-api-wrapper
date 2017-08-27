package com.google.cloudsql.instance;

import static com.google.cloudsql.instance.CloudSqlInstanceTests.getSqlAdminService;
import static org.junit.Assert.assertTrue;

import com.google.cloudsql.instance.model.CloudSqlUser;
import com.google.cloudsql.instance.service.CloudSqlAdminService;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class CloudSqlInstanceUserCreationTest {

  private CloudSqlAdminService sqlAdminService = getSqlAdminService();

  @Test
  @SuppressFBWarnings(value = "DMI_EMPTY_DB_PASSWORD",
      justification = "Root Password cannot be set in v1beta4 version. So using empty password in test")
  public void givenAProjectWithSqlInstance_whenSqlAdminApiCreateUserIsInvoked_thenUserIsCreated() {
    CloudSqlUser createdUser = sqlAdminService.createUser("masterdata_root", "password");
    createdUser.setPassword(
        sqlAdminService.getEncryptedPassword(createdUser.getPassword()));

    CloudSqlUser user = sqlAdminService.getUser("masterdata_root");
    assertTrue(EqualsBuilder.reflectionEquals(user, createdUser));
  }
}
