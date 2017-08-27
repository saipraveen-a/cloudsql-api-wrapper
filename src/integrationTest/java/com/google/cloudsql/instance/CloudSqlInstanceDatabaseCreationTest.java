package com.google.cloudsql.instance;

import static com.google.cloudsql.instance.CloudSqlInstanceTests.getSqlAdminService;
import static org.junit.Assert.assertTrue;

import com.google.cloudsql.instance.model.CloudSqlDatabase;
import com.google.cloudsql.instance.service.CloudSqlAdminService;

import org.junit.Test;

public class CloudSqlInstanceDatabaseCreationTest {

  private static final CloudSqlDatabase DATABASE = new CloudSqlDatabase("createDatabaseTestDb");

  private CloudSqlAdminService sqlAdminService = getSqlAdminService();

  @Test
  public void givenProjectInstanceNameAndSettings_whenSqlAdminApiToCreateDatabaseIsInvoked_thenDatabaseIsCreated() {
    sqlAdminService.createDatabase(DATABASE);

    boolean exists = sqlAdminService.databaseExists(DATABASE);
    assertTrue(exists);
  }
}
