package com.google.cloudsql.instance;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.accessmanager.GoogleAdminSettings;
import com.google.cloudsql.GoogleCloudSqlTestConfiguration;
import com.google.cloudsql.google.GoogleCloudSqlConfiguration;
import com.google.cloudsql.instance.model.CloudSqlDatabase;
import com.google.cloudsql.instance.model.CloudSqlInstance;
import com.google.cloudsql.instance.model.CloudSqlInstanceSettings;
import com.google.cloudsql.instance.service.CloudSqlAdminService;
import com.google.cloudsql.instance.service.GoogleCloudSqlAdminService;
import com.google.cloudsql.instance.service.GoogleCloudSqlSettings;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.concurrent.TimeUnit;

@RunWith(Suite.class)
@Suite.SuiteClasses(
    value = {
        CloudSqlInstanceDatabaseCreationTest.class, CloudSqlInstanceGrantPrivilegesForUserTest.class,
        CloudSqlInstanceGrantReloadPrivilegesForUserTest.class, CloudSqlInstanceTableCreationTest.class,
        CloudSqlInstanceUserCreationTest.class, CloudSqlInstanceImportCsvTest.class})
public final class CloudSqlInstanceTests {

  private static final int INSTANCE_NAME_LENGTH = 10;
  private static final String INSTANCE_NAME = RandomStringUtils.randomAlphabetic(INSTANCE_NAME_LENGTH).toLowerCase();
  private static final CloudSqlDatabase DATABASE = new CloudSqlDatabase("testdatabase");
  private static final int WAIT_TIME_AFTER_INSTANCE_CREATION_IN_SECS = 5;

  private static final GoogleCloudSqlConfiguration TEST_CONFIGURATION = new GoogleCloudSqlTestConfiguration(
      INSTANCE_NAME, DATABASE.getName());

  private static final GoogleCloudSqlAdminService SQL_ADMIN_SERVICE =
      new GoogleCloudSqlAdminService(TEST_CONFIGURATION);

  private static final CloudSqlInstanceSettings TEST_INSTANCE_SETTINGS =
      CloudSqlInstanceSettings.builder().withDefaults().withAuthorizedNetwork("0.0.0.0/0").build();

  private CloudSqlInstanceTests() {}

  @BeforeClass
  public static void suiteSetUp() throws InterruptedException {
    setUpInstanceAndDatabase();
  }

  private static void setUpInstanceAndDatabase() throws InterruptedException {
    CloudSqlInstance instance = new CloudSqlInstance(INSTANCE_NAME, "", getTestInstanceSettings());
    SQL_ADMIN_SERVICE.createInstance(instance);

    assertThat(SQL_ADMIN_SERVICE.instanceExists(INSTANCE_NAME), is(true));

    Thread.sleep(TimeUnit.SECONDS.toMillis(WAIT_TIME_AFTER_INSTANCE_CREATION_IN_SECS));
    SQL_ADMIN_SERVICE.createDatabase(DATABASE);
  }

  public static CloudSqlAdminService getSqlAdminService() {
    return SQL_ADMIN_SERVICE;
  }

  public static GoogleCloudSqlConfiguration getTestConfiguration() {
    return TEST_CONFIGURATION;
  }

  public static CloudSqlInstanceSettings getTestInstanceSettings() {
    return TEST_INSTANCE_SETTINGS;
  }

  public static GoogleAdminSettings getTestProjectSettings() {
    return new GoogleCloudSqlSettings(
        TEST_CONFIGURATION.getProjectId(),
        TEST_CONFIGURATION.getAdminServiceAccount(),
        TEST_CONFIGURATION.getAdminServiceAccountKeyFile());
  }

  @AfterClass
  public static void suiteTearDown() {
    SQL_ADMIN_SERVICE.deleteInstanceIfExists(INSTANCE_NAME);

    assertThat(SQL_ADMIN_SERVICE.instanceExists(INSTANCE_NAME), is(false));
  }
}
