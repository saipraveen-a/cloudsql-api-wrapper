package com.google.cloudsql.instance;

import static com.google.cloudsql.instance.CloudSqlInstanceTests.getSqlAdminService;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.cloudsql.instance.model.CloudSqlDatabase;
import com.google.cloudsql.instance.service.CloudSqlAdminService;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class CloudSqlInstanceGrantPrivilegesForUserTest {

  private static final CloudSqlDatabase NEW_DB_TO_GRANT_PRIVILEGES = new CloudSqlDatabase("newDbToGrantPrivileges");

  private CloudSqlAdminService sqlAdminService = getSqlAdminService();

  @Before
  public void setUp() {
    sqlAdminService.createDatabase(NEW_DB_TO_GRANT_PRIVILEGES);
  }

  @Test
  public void givenAProjectWithInstanceAndDatabase_whenApiToGrantPrivilegesToUserIsInvoked_thenPrivilegesAreGranted() {
    sqlAdminService.grantAllPrivilegesToUserForDatabase("user", NEW_DB_TO_GRANT_PRIVILEGES);

    List<String> userPrivileges = sqlAdminService.getPrivilegesForUser("user");

    List<String> expectedPrivileges = Arrays.asList(
        "GRANT USAGE ON *.* TO 'user'@'%'", "GRANT ALL PRIVILEGES ON `newDbToGrantPrivileges`.* TO 'user'@'%'");
    assertNotNull(userPrivileges);
    assertThat(userPrivileges, is(equalTo(expectedPrivileges)));
  }
}
