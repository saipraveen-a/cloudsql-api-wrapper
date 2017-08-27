package com.google.cloudsql.instance;

import static com.google.cloudsql.instance.CloudSqlInstanceTests.getSqlAdminService;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.cloudsql.instance.service.CloudSqlAdminService;

import org.junit.Test;

import java.util.List;

public class CloudSqlInstanceGrantReloadPrivilegesForUserTest {

  private CloudSqlAdminService sqlAdminService = getSqlAdminService();

  @Test
  public void givenAProjectWithInstance_whenGrantReloadAndUsagePrivilegesToUser_thenPrivilegesAreGranted() {
    sqlAdminService.grantUsageAndReloadPrivilegesToUser("user");

    List<String> userPrivileges = sqlAdminService.getPrivilegesForUser("user");

    List<String> expectedPrivileges = asList(
        "GRANT RELOAD ON *.* TO 'user'@'%' WITH GRANT OPTION",
        "GRANT ALL PRIVILEGES ON `newDbToGrantPrivileges`.* TO 'user'@'%'");
    assertNotNull(userPrivileges);
    assertThat(userPrivileges, is(equalTo(expectedPrivileges)));
  }
}
