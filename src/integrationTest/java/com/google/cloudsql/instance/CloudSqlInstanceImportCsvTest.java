package com.google.cloudsql.instance;

import static com.google.cloudsql.instance.CloudSqlInstanceTests.getSqlAdminService;
import static com.google.cloudsql.instance.CloudSqlInstanceTests.getTestConfiguration;
import static com.google.cloudsql.instance.CloudSqlInstanceTests.getTestProjectSettings;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.accessmanager.GoogleProjectRolesManager;
import com.google.accessmanager.Role;
import com.google.cloudsql.exception.CloudSqlException;
import com.google.cloudsql.google.GoogleCloudSqlConfiguration;
import com.google.cloudsql.instance.matcher.ReflectionMatchers;
import com.google.cloudsql.instance.model.CloudSqlFieldSchema;
import com.google.cloudsql.instance.model.CloudSqlInstance;
import com.google.cloudsql.instance.model.CloudSqlTable;
import com.google.cloudsql.instance.model.CloudSqlTableSchema;
import com.google.cloudsql.instance.model.CsvImportContext;
import com.google.cloudsql.instance.query.QueryInstanceDetails;
import com.google.cloudsql.instance.service.CloudSqlAdminService;
import com.google.cloudsql.parser.CsvParser;
import com.google.cloudsql.query.SelectTestUserQuery;
import com.google.cloudsql.query.TestTableRow;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class CloudSqlInstanceImportCsvTest {

  private static final int VARCHAR_FIELD_LENGTH = 20;
  private static final GoogleCloudSqlConfiguration TEST_CONFIG = getTestConfiguration();
  private static final String GCS_URI = "gs://some-bucket/some-file.csv";
  private static final String TABLE = "testTable_import";
  private static final CloudSqlTableSchema CLOUD_SQL_TABLE_SCHEMA = new CloudSqlTableSchema(
      Arrays.asList(
          new CloudSqlFieldSchema("field1", CloudSqlFieldSchema.DataType.INT),
          new CloudSqlFieldSchema("field2", CloudSqlFieldSchema.DataType.VARCHAR, VARCHAR_FIELD_LENGTH, true, true)));
  private static final CloudSqlTable TEST_TABLE = new CloudSqlTable(TABLE);

  private CloudSqlAdminService sqlAdminService = getSqlAdminService();
  private GoogleProjectRolesManager googleProjectRolesManager = new GoogleProjectRolesManager(getTestProjectSettings());

  @Test
  public void importCsv_givenCsvImportConfig_thenInsertsDataIntoCloudSqlTable() throws IOException, SQLException {
    sqlAdminService.createTable(TEST_TABLE, CLOUD_SQL_TABLE_SCHEMA);
    CsvImportContext csvImportContext = givenCsvImportContext();
    final CloudSqlInstance instance = sqlAdminService.getInstance();
    final String serviceAccountEmailAddress = instance.getServiceAccountEmailAddress();
    googleProjectRolesManager.grantRole(serviceAccountEmailAddress, Role.EDITOR);

    sqlAdminService.importCsv(csvImportContext);

    QueryInstanceDetails queryInstanceDetails = new QueryInstanceDetails(
        TEST_CONFIG.getProjectId(), TEST_CONFIG.getInstance(), TEST_CONFIG.getInstanceRootPassword());
    SelectTestUserQuery selectTestUserQuery = new SelectTestUserQuery(
        queryInstanceDetails, TEST_CONFIG.getDatabase(), TEST_TABLE);
    List<TestTableRow> expectedTestTableRows = getTestTableRows();
    List<TestTableRow> testTableRows = selectTestUserQuery.executeQuery();

    assertNotNull(testTableRows);
    assertThat(testTableRows, ReflectionMatchers.isReflectionEqualTo(expectedTestTableRows));
  }

  @SuppressWarnings("unchecked")
  private List<TestTableRow> getTestTableRows() throws IOException {
    final File file = new File(ClassLoader.getSystemResource("CloudSqlImportTest.csv").getFile());
    return new CsvParser().parse(file, new String[] {"id", "name"}, TestTableRow.class);
  }

  @Test(expected = CloudSqlException.class)
  public void importCsv_givenCsvImportConfig_thenThrowException() {
    CsvImportContext csvImportContext = givenInvalidCsvImportContext();
    final CloudSqlInstance instance = sqlAdminService.getInstance();
    final String serviceAccountEmailAddress = instance.getServiceAccountEmailAddress();
    googleProjectRolesManager.grantRole(serviceAccountEmailAddress, Role.EDITOR);
    sqlAdminService.importCsv(csvImportContext);
  }

  private CsvImportContext givenCsvImportContext() {
    return new CsvImportContext.Builder().setGcsUri(GCS_URI).setCloudSqlTable(TEST_TABLE).build();
  }

  private CsvImportContext givenInvalidCsvImportContext() {
    CloudSqlTable invalidTable = new CloudSqlTable("hello world");
    return new CsvImportContext.Builder().setGcsUri("abc").setCloudSqlTable(invalidTable).build();
  }

  @Test
  public void getDatabase_givenDefaultConfig_thenReturnServiceAccount() {
    final CloudSqlInstance cloudSqlInstance = sqlAdminService.getInstance();
    assertNotNull(cloudSqlInstance.getServiceAccountEmailAddress());
  }
}
