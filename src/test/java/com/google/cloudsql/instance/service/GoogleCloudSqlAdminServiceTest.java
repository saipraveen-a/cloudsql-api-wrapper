package com.google.cloudsql.instance.service;

import static com.google.cloudsql.instance.model.CloudSqlInstanceSettings.getDefaultSettings;
import static com.google.cloudsql.instance.service.GoogleCloudSqlAdminHelper.setSqlAdminClient;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloudsql.exception.CloudSqlException;
import com.google.cloudsql.google.GoogleCloudSqlConfiguration;
import com.google.cloudsql.instance.matcher.ReflectionMatchers;
import com.google.cloudsql.instance.model.CloudSqlDatabase;
import com.google.cloudsql.instance.model.CloudSqlInstance;
import com.google.cloudsql.instance.model.CloudSqlInstanceSettings;
import com.google.cloudsql.instance.model.CloudSqlTable;
import com.google.cloudsql.instance.model.CloudSqlTableSchema;
import com.google.cloudsql.instance.model.CsvImportContext;
import com.google.cloudsql.instance.query.Query;
import com.google.cloudsql.instance.query.QueryFactory;
import com.google.cloudsql.instance.query.QueryInstanceDetails;

import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.model.DatabaseInstance;
import com.google.api.services.sqladmin.model.InstancesListResponse;
import com.google.api.services.sqladmin.model.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GoogleCloudSqlAdminServiceTest {
  private static final String TABLE_NAME = "testtable";
  private static final String GCS_URI = "gs://bucket/file.csv";
  private static final CloudSqlInstanceSettings CLOUD_SQL_INSTANCE_SETTINGS = getDefaultSettings();
  private static final VerificationMode IS_NEVER_INVOKED = times(0);

  @Rule
  // CSOFF: VisibilityModifier
  public ExpectedException expectedException = ExpectedException.none();
  // CSON: VisibilityModifier

  @Mock
  private SQLAdmin sqlAdmin;
  @Mock
  private GoogleCloudSqlConfiguration googleCloudSqlConfiguration;
  @Mock
  private QueryFactory queryFactory;
  @Mock
  private Query query;
  @Mock
  private SQLAdmin.Instances instances;
  @Mock
  private SQLAdmin.Instances.Get request;
  @Mock
  private SQLAdmin.Users users;

  private GoogleCloudSqlAdminService cloudSqlAdminService;

  @Before
  public void setUp() {
    givenSqlAdminInstancesAndUsers();
    setSqlAdminClient(sqlAdmin);

    givenCloudSqlConfig();
    cloudSqlAdminService = new GoogleCloudSqlAdminService(googleCloudSqlConfiguration);
    cloudSqlAdminService.setQueryFactory(queryFactory);
  }

  private void givenSqlAdminInstancesAndUsers() {
    when(sqlAdmin.instances()).thenReturn(instances);
    when(sqlAdmin.users()).thenReturn(users);
    try {
      when(sqlAdmin.instances().get(anyString(), anyString())).thenReturn(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void givenCloudSqlConfig() {
    when(googleCloudSqlConfiguration.getProjectId()).thenReturn("someProject");
    when(googleCloudSqlConfiguration.getInstance()).thenReturn("someInstance");
    when(googleCloudSqlConfiguration.getDatabase()).thenReturn("someDatabase");
  }

  @Test
  public void instanceExists_whenListInstancesRequestFailure_thenThrowsCloudSqlException() throws IOException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Error while getting instance : [someInstance]");

    doThrow(IOException.class).when(instances).list(anyString());

    cloudSqlAdminService.instanceExists("someInstance");
  }

  @Test
  public void createInstance_givenInstanceAlreadyExists_thenThrowsCloudSqlException() throws IOException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Instance already exists - name : [someInstance]");

    givenEnvWithInstances("someInstance");
    CloudSqlInstance cloudSqlInstance = new CloudSqlInstance("someInstance", "", CLOUD_SQL_INSTANCE_SETTINGS);

    cloudSqlAdminService.createInstance(cloudSqlInstance);
  }

  private void givenEnvWithInstances(String... instanceNames) throws IOException {
    SQLAdmin.Instances.List list = mock(SQLAdmin.Instances.List.class);
    doReturn(list).when(instances).list(anyString());

    InstancesListResponse instancesListResponse = new InstancesListResponse();
    instancesListResponse.setItems(givenInstances(instanceNames));

    doReturn(instancesListResponse).when(list).execute();
  }

  private List<DatabaseInstance> givenInstances(String... instanceNames) {
    List<DatabaseInstance> databaseInstanceList = new ArrayList<>();
    for (String instanceName : instanceNames) {
      DatabaseInstance databaseInstance = new DatabaseInstance();
      databaseInstance.setName(instanceName);
      databaseInstanceList.add(databaseInstance);
    }
    return databaseInstanceList;
  }

  @Test
  public void createInstance_whenInstanceInsertRequestFailure_thenThrowsCloudSqlException() throws IOException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Error while creating instance : [someInstance] in project: [someProject]");

    givenEnvWithInstances();
    doThrow(IOException.class).when(instances).insert(anyString(), any(DatabaseInstance.class));
    CloudSqlInstance cloudSqlInstance = new CloudSqlInstance("someInstance", "", CLOUD_SQL_INSTANCE_SETTINGS);

    cloudSqlAdminService.createInstance(cloudSqlInstance);
  }

  @Test
  public void deleteInstanceIfExists_whenInstanceDeleteRequestFailure_thenThrowsCloudSqlException() throws IOException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Error while deleting cloud sql instance : [someInstance]");

    givenEnvWithInstances("someInstance");
    doThrow(IOException.class).when(instances).delete(anyString(), eq("someInstance"));

    cloudSqlAdminService.deleteInstanceIfExists("someInstance");
  }

  @Test
  public void deleteInstanceIfExists_whenInstanceDoesntExist_thenDoesnotInvokeDeleteRequest() throws IOException {
    givenEnvWithInstances();

    cloudSqlAdminService.deleteInstanceIfExists("someInstance");

    verify(instances, IS_NEVER_INVOKED).delete(anyString(), anyString());
  }

  @Test
  public void createUser_whenInsertUserFailure_thenThrowsCloudSqlException() throws IOException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Error while creating user: [someUser], in instance : [someInstance]");

    doThrow(IOException.class).when(users).insert(anyString(), anyString(), any(User.class));

    cloudSqlAdminService.createUser("someUser", "userPassword");
  }

  @Test
  public void getUser_whenUserDetailsQueryFails_thenThrowsCloudSqlException() throws IOException, SQLException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Error while get user: [someUser] in instance: [someInstance]");

    doReturn(query).when(queryFactory).getUserDetailQuery(any(QueryInstanceDetails.class), anyString());
    doThrow(SQLException.class).when(query).executeQuery();

    cloudSqlAdminService.getUser("someUser");
  }

  @Test
  public void getEncryptedPassword_whenEncyrptedPasswordQueryFails_thenThrowsCloudSqlException()
      throws IOException, SQLException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage(
        "Error while getting encrypted password for: [somePassword] in instance: [someInstance]");

    doReturn(query).when(queryFactory).getEncryptedPasswordQuery(any(QueryInstanceDetails.class), anyString());
    doThrow(SQLException.class).when(query).executeQuery();

    cloudSqlAdminService.getEncryptedPassword("somePassword");
  }

  @Test
  public void createDatabase_whenCreateDatabaseQueryFails_thenThrowsCloudSqlException()
      throws IOException, SQLException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Error while creating database: [someNewDatabase] in instance: [someInstance]");

    doReturn(query).when(queryFactory).getCreateDatabaseQuery(any(QueryInstanceDetails.class), anyString());
    doThrow(SQLException.class).when(query).executeQuery();

    cloudSqlAdminService.createDatabase(new CloudSqlDatabase("someNewDatabase"));
  }

  @Test
  public void databaseExists_whenGetDatabaseQueryFails_thenThrowsCloudSqlException() throws IOException, SQLException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Error while getting database: [someDatabase] in instance: [someInstance]");

    doReturn(query).when(queryFactory).getDatabaseQuery(any(QueryInstanceDetails.class), anyString());
    doThrow(SQLException.class).when(query).executeQuery();

    cloudSqlAdminService.databaseExists(new CloudSqlDatabase("someDatabase"));
  }

  @Test
  public void databaseExists_whenGetDatabaseQueryReturnsNull_thenThrowsCloudSqlException()
      throws IOException, SQLException {
    doReturn(query).when(queryFactory).getDatabaseQuery(any(QueryInstanceDetails.class), eq("someDatabase"));
    doReturn(null).when(query).executeQuery();

    boolean databaseExists = cloudSqlAdminService.databaseExists(new CloudSqlDatabase("someDatabase"));

    assertThat(databaseExists, is(false));
  }

  @Test
  public void databaseExists_whenGetDatabaseQueryReturnsDatabaseName_thenThrowsCloudSqlException()
      throws IOException, SQLException {
    doReturn(query).when(queryFactory).getDatabaseQuery(any(QueryInstanceDetails.class), eq("someDatabase"));
    doReturn("someDatabase").when(query).executeQuery();

    boolean databaseExists = cloudSqlAdminService.databaseExists(new CloudSqlDatabase("someDatabase"));

    assertThat(databaseExists, is(true));
  }

  @Test
  public void grantAllPrivilegesToUserForDatabase_whenGrantQueryFails_thenThrowsCloudSqlException()
      throws IOException, SQLException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Error while granting privileges to user: [someUser] on database: "
        + "[someOtherDatabase]");

    doReturn(query).when(queryFactory).getGrantAllPrivilegesToUserOnDatabaseQuery(
        any(QueryInstanceDetails.class), anyString(), anyString());
    doThrow(SQLException.class).when(query).executeQuery();

    cloudSqlAdminService.grantAllPrivilegesToUserForDatabase("someUser", new CloudSqlDatabase("someOtherDatabase"));
  }

  @Test
  public void getPrivilegesForUser_whenGetPrivilegesQueryFails_thenThrowsCloudSqlException()
      throws IOException, SQLException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Error while getting privileges for user: [someUser]");

    doReturn(query).when(queryFactory).getUserPrivilegesQuery(any(QueryInstanceDetails.class), anyString());
    doThrow(SQLException.class).when(query).executeQuery();

    cloudSqlAdminService.getPrivilegesForUser("someUser");
  }

  @Test
  public void grantUsageAndReloadPrivilegesToUser_whenGrantQueryFails_thenThrowsCloudSqlException()
      throws IOException, SQLException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage(
        "Error while granting reload and usage privileges to user: [someUser] in instance: [someInstance]");

    doReturn(query).when(queryFactory).getGrantReloadPrivilegesToUserOnDatabaseQuery(
        any(QueryInstanceDetails.class), anyString());
    doThrow(SQLException.class).when(query).executeQuery();

    cloudSqlAdminService.grantUsageAndReloadPrivilegesToUser("someUser");
  }

  @Test
  public void createTable_whenCreateTableQueryFails_thenThrowsCloudSqlException() throws IOException, SQLException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage("Error while creating table : [someTable]");

    doReturn(query).when(queryFactory).getCreateTableQuery(
        any(QueryInstanceDetails.class),
        any(CloudSqlDatabase.class),
        any(CloudSqlTable.class),
        any(CloudSqlTableSchema.class));
    doThrow(SQLException.class).when(query).executeQuery();

    cloudSqlAdminService.createTable(new CloudSqlTable("someTable"), mock(CloudSqlTableSchema.class));
  }

  @Test
  public void tableExists_whenGetTableQueryFails_thenThrowsCloudSqlException() throws IOException, SQLException {
    expectedException.expect(CloudSqlException.class);
    expectedException.expectMessage(
        "Error while getting table: [someTable] in database: [someDatabase] in instance: [someInstance]");

    doReturn(query).when(queryFactory).getFetchTableQuery(
        any(QueryInstanceDetails.class), any(CloudSqlDatabase.class), any(CloudSqlTable.class));
    doThrow(SQLException.class).when(query).executeQuery();

    cloudSqlAdminService.tableExists(new CloudSqlTable("someTable"));
  }

  @Test
  public void tableExists_whenGetTableQueryReturnsTableName_thenReturnsTrue() throws IOException, SQLException {
    doReturn(query).when(queryFactory).getFetchTableQuery(
        any(QueryInstanceDetails.class), any(CloudSqlDatabase.class), refEq(new CloudSqlTable("someTable")));
    doReturn("someTable").when(query).executeQuery();

    boolean tableExists = cloudSqlAdminService.tableExists(new CloudSqlTable("someTable"));

    assertThat(tableExists, is(true));
  }

  @Test
  public void tableExists_whenGetTableQueryReturnsNull_thenReturnsFalse() throws IOException, SQLException {
    doReturn(query).when(queryFactory).getFetchTableQuery(
        any(QueryInstanceDetails.class), any(CloudSqlDatabase.class), refEq(new CloudSqlTable("someTable")));
    doReturn(null).when(query).executeQuery();

    boolean tableExists = cloudSqlAdminService.tableExists(new CloudSqlTable("someTable"));

    assertThat(tableExists, is(false));
  }

  @Test(expected = CloudSqlException.class)
  public void importCsv_givenInstanceDatabaseAndTable_whenIoExceptionIsThrown_thenThrowException() {
    CsvImportContext csvImportContext = givenConfig();
    doThrow(IOException.class).when(sqlAdmin).instances();

    cloudSqlAdminService.importCsv(csvImportContext);

    verify(cloudSqlAdminService).importCsv(refEq(csvImportContext));
  }

  private CsvImportContext givenConfig() {
    CloudSqlTable table = new CloudSqlTable(TABLE_NAME);
    return new CsvImportContext.Builder().setGcsUri(GCS_URI).setCloudSqlTable(table).build();
  }

  @Test(expected = NullPointerException.class)
  public void importCsv_givenNullData_thenThrowException() {
    CsvImportContext csvImportContext = givenNullConfig();

    cloudSqlAdminService.importCsv(csvImportContext);

    verify(cloudSqlAdminService).importCsv(refEq(csvImportContext));
  }

  private CsvImportContext givenNullConfig() {
    CloudSqlTable table = new CloudSqlTable(null);
    return new CsvImportContext.Builder().setGcsUri(null).setCloudSqlTable(table).build();
  }

  @Test
  public void getInstance_givenDatabaseInstance_thenReturnDatabaseInstance() throws IOException {
    String serviceAccountEmailAddress = "helloworld@abc.com";
    DatabaseInstance instance = new DatabaseInstance();
    instance.setServiceAccountEmailAddress(serviceAccountEmailAddress);

    when(request.execute()).thenReturn(instance);
    final CloudSqlInstance cloudSqlInstance = cloudSqlAdminService.getInstance();
    verify(request).execute();

    CloudSqlInstance expectedCloudSqlInstance = new CloudSqlInstance(
        "someInstance", serviceAccountEmailAddress, CLOUD_SQL_INSTANCE_SETTINGS);

    assertNotNull(cloudSqlInstance);
    assertThat(expectedCloudSqlInstance, ReflectionMatchers.isReflectionEqualTo(cloudSqlInstance));
  }

  @Test(expected = CloudSqlException.class)
  public void getInstance_givenDatabaseInstance_whenIoException_thenThrowCloudSqlException() throws IOException {
    when(sqlAdmin.instances().get(anyString(), anyString())).thenThrow(IOException.class);
    cloudSqlAdminService.getInstance();
  }
}
