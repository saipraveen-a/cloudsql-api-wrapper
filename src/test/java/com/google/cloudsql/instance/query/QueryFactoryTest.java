package com.google.cloudsql.instance.query;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;

import com.google.cloudsql.instance.model.CloudSqlDatabase;
import com.google.cloudsql.instance.model.CloudSqlFieldSchema;
import com.google.cloudsql.instance.model.CloudSqlTable;
import com.google.cloudsql.instance.model.CloudSqlTableSchema;

import org.junit.Before;
import org.junit.Test;

public class QueryFactoryTest {

  private static final String DEV_PROJECT = "my-project";
  private static final String INSTANCE_NAME = "instance-name";
  private static final String ROOT_PASSWORD = "password";
  private static final String PASSWORD_TO_ENCRYPT = "password";
  private static final String DATABASE_NAME = "testdatabase";
  private static final String USER = "testuser";

  private QueryFactory queryFactory;
  private Query userDetailQuery;
  private Query encryptedPasswordQuery;
  private Query createDatabaseQuery;
  private Query getDatabaseQuery;
  private Query getCreateTableQuery;
  private Query getTableQuery;
  private Query grantAllPrivilegesQuery;
  private Query getUserPrivilegesQuery;
  private Query grantReloadPrivilegesQuery;
  private QueryInstanceDetails queryInstanceDetails;

  private CloudSqlDatabase cloudSqlDatabase = new CloudSqlDatabase(DATABASE_NAME);
  private CloudSqlTable cloudSqlTable = new CloudSqlTable("testTable");
  private CloudSqlTableSchema cloudSqlTableSchema = new CloudSqlTableSchema(
      singletonList(
          new CloudSqlFieldSchema(
              "testField", CloudSqlFieldSchema.DataType.INT)));

  @Before
  public void setUp() throws Exception {
    queryFactory = new QueryFactory();
    queryInstanceDetails = new QueryInstanceDetails(DEV_PROJECT, INSTANCE_NAME, ROOT_PASSWORD);
  }

  @Test
  public void getUserDetailQuery_givenProperties_thenQueryIsNotNull() {
    userDetailQuery = queryFactory.getUserDetailQuery(queryInstanceDetails, USER);

    assertNotNull(userDetailQuery);
  }

  @Test
  public void getUserDetailQuery_givenProperties_thenQueryIsOfExpectedType() {
    userDetailQuery = queryFactory.getUserDetailQuery(queryInstanceDetails, USER);

    assertThat(userDetailQuery, is(instanceOf(UserDetailQuery.class)));
  }

  @Test
  public void getEncryptedPasswordQuery_givenProperties_thenQueryIsNotNull() {
    encryptedPasswordQuery = queryFactory.getEncryptedPasswordQuery(queryInstanceDetails, PASSWORD_TO_ENCRYPT);

    assertNotNull(encryptedPasswordQuery);
  }

  @Test
  public void getEncryptedPasswordQuery_givenProperties_thenQueryIsOfExpectedType() {
    encryptedPasswordQuery = queryFactory.getEncryptedPasswordQuery(queryInstanceDetails, PASSWORD_TO_ENCRYPT);

    assertThat(encryptedPasswordQuery, is(instanceOf(EncryptedPasswordQuery.class)));
  }

  @Test
  public void getCreateDatabaseQuery_givenProperties_thenQueryIsNotNull() {
    createDatabaseQuery = queryFactory.getCreateDatabaseQuery(queryInstanceDetails, PASSWORD_TO_ENCRYPT);

    assertNotNull(createDatabaseQuery);
  }

  @Test
  public void getCreateDatabaseQuery_givenProperties_thenQueryIsOfExpectedType() {
    createDatabaseQuery = queryFactory.getCreateDatabaseQuery(queryInstanceDetails, PASSWORD_TO_ENCRYPT);

    assertThat(createDatabaseQuery, is(instanceOf(CreateDatabaseQuery.class)));
  }

  @Test
  public void getDatabaseQuery_givenProperties_thenQueryIsNotNull() {
    getDatabaseQuery = queryFactory.getDatabaseQuery(queryInstanceDetails, DATABASE_NAME);

    assertNotNull(getDatabaseQuery);
  }

  @Test
  public void getDatabaseQuery_givenProperties_thenQueryIsOfExpectedType() {
    getDatabaseQuery = queryFactory.getDatabaseQuery(queryInstanceDetails, DATABASE_NAME);

    assertThat(getDatabaseQuery, is(instanceOf(GetDatabaseQuery.class)));
  }

  @Test
  public void getCreateTableQuery_givenProperties_thenQueryIsNotNull() {
    getCreateTableQuery = queryFactory.getCreateTableQuery(
        queryInstanceDetails, cloudSqlDatabase, cloudSqlTable, cloudSqlTableSchema);

    assertNotNull(getCreateTableQuery);
  }

  @Test
  public void getCreateTableQuery_givenProperties_thenQueryIsOfExpectedType() {
    getCreateTableQuery = queryFactory.getCreateTableQuery(
        queryInstanceDetails, cloudSqlDatabase, cloudSqlTable, cloudSqlTableSchema);

    assertThat(getCreateTableQuery, is(instanceOf(CreateTableQuery.class)));
  }

  @Test
  public void getGrantAllPrivilegesToUserOnDatabaseQuery_givenProperties_thenQueryIsNotNull() {
    grantAllPrivilegesQuery = queryFactory.getGrantAllPrivilegesToUserOnDatabaseQuery(
        queryInstanceDetails, USER, DATABASE_NAME);

    assertNotNull(grantAllPrivilegesQuery);
  }

  @Test
  public void getGrantAllPrivilegesToUserOnDatabaseQuery_givenProperties_thenQueryIsOfExpectedType() {
    grantAllPrivilegesQuery = queryFactory.getGrantAllPrivilegesToUserOnDatabaseQuery(
        queryInstanceDetails, USER, DATABASE_NAME);

    assertThat(grantAllPrivilegesQuery, is(instanceOf(GrantAllPrivilegesQuery.class)));
  }

  @Test
  public void getUserPrivilegesQuery_givenProperties_thenQueryIsNotNull() {
    getUserPrivilegesQuery = queryFactory.getUserPrivilegesQuery(queryInstanceDetails, USER);

    assertNotNull(getUserPrivilegesQuery);
  }

  @Test
  public void getUserPrivilegesQuery_givenProperties_thenQueryIsOfExpectedType() {
    getUserPrivilegesQuery = queryFactory.getUserPrivilegesQuery(queryInstanceDetails, USER);

    assertThat(getUserPrivilegesQuery, is(instanceOf(GetUserPrivilegesQuery.class)));
  }

  @Test
  public void getGrantReloadPrivilegesToUserOnDatabaseQuery_givenProperties_thenQueryIsNotNull() {
    grantReloadPrivilegesQuery = queryFactory.getGrantReloadPrivilegesToUserOnDatabaseQuery(
        queryInstanceDetails, USER);

    assertNotNull(grantReloadPrivilegesQuery);
  }

  @Test
  public void getGrantReloadPrivilegesToUserOnDatabaseQuery_givenProperties_thenQueryIsOfExpectedType() {
    grantReloadPrivilegesQuery = queryFactory.getGrantReloadPrivilegesToUserOnDatabaseQuery(
        queryInstanceDetails, USER);

    assertThat(grantReloadPrivilegesQuery, is(instanceOf(GrantUsageAndReloadPrivilegesQuery.class)));
  }

  @Test
  public void getTableQuery_givenProperties_thenQueryIsNotNull() {
    getTableQuery = queryFactory.getFetchTableQuery(
        queryInstanceDetails, cloudSqlDatabase, cloudSqlTable);

    assertNotNull(getTableQuery);
  }

  @Test
  public void getTableQuery_givenProperties_thenQueryIsOfExpectedType() {
    getTableQuery = queryFactory.getFetchTableQuery(queryInstanceDetails, cloudSqlDatabase, cloudSqlTable);

    assertThat(getTableQuery, is(instanceOf(GetTableQuery.class)));
  }
}
