package com.google.cloudsql.instance.service;

import static com.google.cloudsql.instance.service.GoogleCloudSqlAdminHelper.getDatabaseInstanceWithSettings;
import static com.google.cloudsql.instance.service.GoogleCloudSqlAdminHelper.getSqlAdmin;
import static com.google.cloudsql.instance.service.GoogleCloudSqlAdminHelper.getUserContent;
import static com.google.cloudsql.instance.service.GoogleCloudSqlAdminHelper.waitUntilOperationIsDone;
import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.google.cloudsql.exception.CloudSqlException;
import com.google.cloudsql.google.GoogleCloudSqlConfiguration;
import com.google.cloudsql.instance.model.CloudSqlDatabase;
import com.google.cloudsql.instance.model.CloudSqlInstance;
import com.google.cloudsql.instance.model.CloudSqlInstanceSettings;
import com.google.cloudsql.instance.model.CloudSqlTable;
import com.google.cloudsql.instance.model.CloudSqlTableSchema;
import com.google.cloudsql.instance.model.CloudSqlUser;
import com.google.cloudsql.instance.model.CsvImportContext;
import com.google.cloudsql.instance.query.Query;
import com.google.cloudsql.instance.query.QueryFactory;
import com.google.cloudsql.instance.query.QueryInstanceDetails;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.SQLAdmin.Instances.Delete;
import com.google.api.services.sqladmin.SQLAdmin.Instances.Insert;
import com.google.api.services.sqladmin.model.DatabaseInstance;
import com.google.api.services.sqladmin.model.ImportContext;
import com.google.api.services.sqladmin.model.InstancesImportRequest;
import com.google.api.services.sqladmin.model.InstancesListResponse;
import com.google.api.services.sqladmin.model.Operation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class GoogleCloudSqlAdminService implements CloudSqlAdminService {
  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudSqlAdminService.class);
  private static final String ANY_HOST = "%";

  private final GoogleCloudSqlConfiguration googleCloudSqlConfiguration;
  private QueryFactory queryFactory;

  public GoogleCloudSqlAdminService(GoogleCloudSqlConfiguration googleCloudSqlConfiguration) {
    this.googleCloudSqlConfiguration = googleCloudSqlConfiguration;
    this.queryFactory = new QueryFactory();
  }

  @Override
  public CloudSqlInstance createInstance(CloudSqlInstance instance) {
    String projectId = googleCloudSqlConfiguration.getProjectId();
    String instanceName = instance.getName();
    CloudSqlInstanceSettings settings = instance.getCloudSqlInstanceSettings();

    if (instanceExists(instance.getName())) {
      throw new CloudSqlException(format("Instance already exists - name : [%s]", instanceName));
    }

    try {
      Insert insertRequest = sqlAdmin().instances().insert(
          projectId, getDatabaseInstanceWithSettings(
              instanceName, projectId, settings));
      Operation operation = insertRequest.execute();

      waitUntilOperationIsDone(projectId, operation);
    } catch (IOException e) {
      throw new CloudSqlException(
          format("Error while creating instance : [%s] in project: [%s]", instanceName, projectId), e);
    }
    return instance;
  }

  private SQLAdmin sqlAdmin() {
    return getSqlAdmin(googleCloudSqlConfiguration);
  }

  @Override
  public CloudSqlInstance getInstance() {
    CloudSqlInstance cloudSqlInstance;
    String projectId = googleCloudSqlConfiguration.getProjectId();
    String instance = googleCloudSqlConfiguration.getInstance();
    try {
      DatabaseInstance databaseInstance = getDatabaseInstance(projectId, instance);
      String serviceAccountEmailAddress = databaseInstance.getServiceAccountEmailAddress();

      CloudSqlInstanceSettings cloudSqlInstanceSettings = CloudSqlInstanceSettings.builder().withDefaults().build();
      cloudSqlInstance = new CloudSqlInstance(instance, serviceAccountEmailAddress, cloudSqlInstanceSettings);
    } catch (IOException e) {
      throw new CloudSqlException(
          String.format("Failed to read instance for ProjectId: %s, instance: %s", projectId, instance), e);
    }
    return cloudSqlInstance;
  }

  private DatabaseInstance getDatabaseInstance(String projectId, String instance) throws IOException {
    return sqlAdmin().instances().get(projectId, instance).execute();
  }

  @Override
  public boolean instanceExists(String instanceName) {
    String projectId = googleCloudSqlConfiguration.getProjectId();

    LOGGER.info("Checking if instance [{}] exists in project [{}]", instanceName, projectId);
    try {
      SQLAdmin.Instances.List listRequest = sqlAdmin().instances().list(projectId);
      InstancesListResponse instances = listRequest.execute();
      return checkInstanceExistsInList(instances, instanceName);
    } catch (IOException e) {
      throw new CloudSqlException(format("Error while getting instance : [%s]", instanceName), e);
    }
  }

  private boolean checkInstanceExistsInList(InstancesListResponse instances, String instanceName) {
    List<DatabaseInstance> databaseInstances = instances.getItems();
    if (isNotEmpty(databaseInstances)) {
      for (DatabaseInstance databaseInstance : databaseInstances) {
        if (StringUtils.equals(databaseInstance.getName(), instanceName)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void deleteInstanceIfExists(String instanceName) {
    String projectId = googleCloudSqlConfiguration.getProjectId();
    LOGGER.info("Deleting instance [{}] in project [{}]", instanceName, projectId);

    try {
      if (instanceExists(instanceName)) {
        Delete deleteRequest = sqlAdmin().instances().delete(projectId, instanceName);
        Operation deleteOperation = deleteRequest.execute();
        waitUntilOperationIsDone(projectId, deleteOperation);
      } else {
        LOGGER.info("No instance exists with name: {}", instanceName);
      }
    } catch (IOException e) {
      throw new CloudSqlException(format("Error while deleting cloud sql instance : [%s]", instanceName), e);
    }
  }

  @Override
  public CloudSqlUser createUser(String user, String userPassword) {
    String projectId = googleCloudSqlConfiguration.getProjectId();
    String instance = googleCloudSqlConfiguration.getInstance();
    CloudSqlUser cloudSqlUser = new CloudSqlUser(user, userPassword, ANY_HOST);
    LOGGER.info("Creating user: [{}] in instance: [{}]", user, instance);

    try {
      SQLAdmin.Users.Insert insertUserRequest = sqlAdmin().users().insert(
          projectId, instance, getUserContent(
              user, userPassword));
      Operation operation = insertUserRequest.execute();
      waitUntilOperationIsDone(googleCloudSqlConfiguration.getProjectId(), operation);
    } catch (IOException e) {
      throw new CloudSqlException(format("Error while creating user: [%s], in instance : [%s]", user, instance), e);
    }
    return cloudSqlUser;
  }

  @Override
  public CloudSqlUser getUser(String user) {
    String instance = googleCloudSqlConfiguration.getInstance();

    LOGGER.info("Getting user: [{}] in instance: [{}]", user, instance);
    try {
      Query<CloudSqlUser> userDetailsFetcher = queryFactory.getUserDetailQuery(defaultInstanceDetails(), user);
      return userDetailsFetcher.executeQuery();
    } catch (SQLException e) {
      throw new CloudSqlException(format("Error while get user: [%s] in instance: [%s]", user, instance), e);
    }
  }

  @Override
  public String getEncryptedPassword(String passwordToEncrypt) {
    String instance = googleCloudSqlConfiguration.getInstance();

    LOGGER.info("Getting Encrypted Password for: {} in instance: {}", passwordToEncrypt, instance);
    try {
      Query<String> encryptedPasswordFetcher = queryFactory.getEncryptedPasswordQuery(
          defaultInstanceDetails(), passwordToEncrypt);
      return encryptedPasswordFetcher.executeQuery();
    } catch (SQLException e) {
      throw new CloudSqlException(
          format("Error while getting encrypted password for: [%s] in instance: [%s]", passwordToEncrypt, instance), e);
    }
  }

  private QueryInstanceDetails defaultInstanceDetails() {
    return new QueryInstanceDetails(
        googleCloudSqlConfiguration.getProjectId(),
        googleCloudSqlConfiguration.getInstance(),
        googleCloudSqlConfiguration.getInstanceRootPassword());
  }

  @Override
  public void createDatabase(CloudSqlDatabase cloudSqlDatabase) {
    String instanceName = googleCloudSqlConfiguration.getInstance();
    String databaseName = cloudSqlDatabase.getName();

    LOGGER.info("Creating database: {} in instance: {}", databaseName, instanceName);
    try {
      Query createDatabase = queryFactory.getCreateDatabaseQuery(defaultInstanceDetails(), databaseName);
      createDatabase.executeQuery();
    } catch (SQLException e) {
      throw new CloudSqlException(
          format("Error while creating database: [%s] in instance: [%s]", databaseName, instanceName), e);
    }
  }

  @Override
  public boolean databaseExists(CloudSqlDatabase cloudSqlDatabase) {
    String instance = googleCloudSqlConfiguration.getInstance();
    String database = cloudSqlDatabase.getName();

    LOGGER.info("Getting database: {} in instance: {}", database, instance);
    try {
      Query fetchDatabaseQuery = queryFactory.getDatabaseQuery(defaultInstanceDetails(), database);
      return StringUtils.equals((String) fetchDatabaseQuery.executeQuery(), database);
    } catch (SQLException e) {
      throw new CloudSqlException(
          format("Error while getting database: [%s] in instance: [%s]", database, instance), e);
    }
  }

  @Override
  public void grantAllPrivilegesToUserForDatabase(String user, CloudSqlDatabase cloudSqlDatabase) {
    String instance = googleCloudSqlConfiguration.getInstance();
    String database = cloudSqlDatabase.getName();

    LOGGER.info("Granting all privileges to user: {} for database: {} in instance: {}", user, database, instance);
    try {
      Query grantPrivilegesQuery = queryFactory.getGrantAllPrivilegesToUserOnDatabaseQuery(
          defaultInstanceDetails(), user, database);
      grantPrivilegesQuery.executeQuery();
    } catch (SQLException e) {
      throw new CloudSqlException(
          format("Error while granting privileges to user: [%s] on database: [%s]", user, database), e);
    }
  }

  @Override
  public List<String> getPrivilegesForUser(String user) {
    String instance = googleCloudSqlConfiguration.getInstance();

    LOGGER.info("Getting privileges for user: {} in instance: {}", user, instance);
    try {
      Query<List<String>> getUserPrivilegesQuery = queryFactory.getUserPrivilegesQuery(defaultInstanceDetails(), user);
      return getUserPrivilegesQuery.executeQuery();
    } catch (SQLException e) {
      throw new CloudSqlException(format("Error while getting privileges for user: [%s]", user), e);
    }
  }

  @Override
  public void grantUsageAndReloadPrivilegesToUser(String user) {
    String instance = googleCloudSqlConfiguration.getInstance();

    LOGGER.info("Granting reload and usage privileges to user: {} in instance: {}", user, instance);
    try {
      Query grantPrivilegesQuery = queryFactory.getGrantReloadPrivilegesToUserOnDatabaseQuery(
          defaultInstanceDetails(), user);
      grantPrivilegesQuery.executeQuery();
    } catch (SQLException e) {
      throw new CloudSqlException(
          format("Error while granting reload and usage privileges to user: [%s] in instance: [%s]", user, instance),
          e);
    }
  }

  @Override
  public void createTable(CloudSqlTable cloudSqlTable, CloudSqlTableSchema cloudSqlTableSchema) {
    LOGGER.info("Creating table {}", cloudSqlTable.getName());
    String database = googleCloudSqlConfiguration.getDatabase();

    try {
      Query createTableQuery = queryFactory.getCreateTableQuery(
          defaultInstanceDetails(), new CloudSqlDatabase(database), cloudSqlTable, cloudSqlTableSchema);
      createTableQuery.executeQuery();
    } catch (SQLException e) {
      throw new CloudSqlException(format("Error while creating table : [%s]", cloudSqlTable.getName()), e);
    }
  }

  @Override
  public boolean tableExists(CloudSqlTable cloudSqlTable) {
    String database = googleCloudSqlConfiguration.getDatabase();
    String tableName = cloudSqlTable.getName();

    LOGGER.info(
        "Checking if table: {} exists in database: {} in instance: {}",
        tableName,
        database,
        googleCloudSqlConfiguration.getInstance());
    try {
      Query fetchTableQuery = queryFactory.getFetchTableQuery(
          defaultInstanceDetails(), new CloudSqlDatabase(database), cloudSqlTable);
      return StringUtils.equals((String) fetchTableQuery.executeQuery(), (tableName));
    } catch (SQLException e) {
      throw new CloudSqlException(
          format(
              "Error while getting table: [%s] in database: [%s] in instance: [%s]",
              tableName,
              database,
              googleCloudSqlConfiguration.getInstance()), e);
    }
  }

  @Override
  public void importCsv(CsvImportContext csvImportContext) {
    String projectId = googleCloudSqlConfiguration.getProjectId();
    String instance = googleCloudSqlConfiguration.getInstance();
    String database = googleCloudSqlConfiguration.getDatabase();
    String tableName = csvImportContext.getCloudSqlTable().getName();
    String gcsUri = csvImportContext.getGcsUri();

    Preconditions.checkNotNull(gcsUri);
    Preconditions.checkNotNull(tableName);

    LOGGER.info("Importing csv [{}] into table [{}]", gcsUri, tableName);

    Operation response;
    try {
      DatabaseInstance databaseInstance = getDatabaseInstance(projectId, instance);
      response = sqlAdmin().instances().sqladminImport(
          projectId, instance, buildImportContext(
              tableName, gcsUri, database)).execute();
    } catch (IOException e) {
      throw new CloudSqlException(format("Failed to load csv : [%s] to table : [%s]", gcsUri, tableName), e);
    }

    waitUntilOperationIsDone(projectId, response);
  }

  private InstancesImportRequest buildImportContext(String table, String gcsUri, String database) {
    ImportContext.CsvImportOptions csvImportOptions = new ImportContext.CsvImportOptions();
    csvImportOptions.setTable(table);

    ImportContext importContext = new ImportContext();
    importContext.setKind("sql#importContext");
    importContext.setFileType("CSV");
    importContext.setUri(gcsUri);
    importContext.setDatabase(database);
    importContext.setCsvImportOptions(csvImportOptions);

    InstancesImportRequest importRequest = new InstancesImportRequest();
    return importRequest.setImportContext(importContext);
  }

  // For unit test purposes only
  void setQueryFactory(QueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }
}
