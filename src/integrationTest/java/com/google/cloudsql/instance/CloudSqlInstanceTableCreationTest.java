package com.google.cloudsql.instance;

import static com.google.cloudsql.instance.CloudSqlInstanceTests.getSqlAdminService;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.BIGINT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.BLOB;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.CHAR;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.DATE;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.DATETIME;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.DECIMAL;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.DOUBLE;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.FLOAT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.INT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.LONGBLOB;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.LONGTEXT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.MEDIUMBLOB;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.MEDIUMINT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.MEDIUMTEXT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.SMALLINT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.TEXT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.TIME;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.TIMESTAMP;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.TINYBLOB;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.TINYINT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.TINYTEXT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.VARCHAR;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.YEAR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.cloudsql.instance.model.CloudSqlFieldSchema;
import com.google.cloudsql.instance.model.CloudSqlTable;
import com.google.cloudsql.instance.model.CloudSqlTablePrimaryKey;
import com.google.cloudsql.instance.model.CloudSqlTableSchema;
import com.google.cloudsql.instance.service.CloudSqlAdminService;

import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class CloudSqlInstanceTableCreationTest {

  private static final CloudSqlTable TEST_TABLE = new CloudSqlTable("testTable");
  private static final CloudSqlTable TEST_TABLE_WITH_DEFAULTS = new CloudSqlTable("testTable2");
  private static final CloudSqlTable TEST_TABLE_WITH_PRIMARY_KEY = new CloudSqlTable("testTable3");

  private CloudSqlAdminService sqlAdminService = getSqlAdminService();

  @Test
  public void givenAProjectWithSqlInstance_whenSqlAdminApiCreateTableIsInvoked_thenTableIsCreated() {
    CloudSqlTableSchema cloudSqlTableSchema = givenSchema(givenFields());

    sqlAdminService.createTable(TEST_TABLE, cloudSqlTableSchema);

    boolean tableCreated = sqlAdminService.tableExists(TEST_TABLE);
    assertThat(tableCreated, is(true));
  }

  //CSOFF: MagicNumber
  private CloudSqlFieldSchema[] givenFields() {
    return new CloudSqlFieldSchema[] {
        new CloudSqlFieldSchema("intField", INT), new CloudSqlFieldSchema("tinyIntField", TINYINT),
        new CloudSqlFieldSchema("bigIntField", BIGINT), new CloudSqlFieldSchema("smallIntField", SMALLINT),
        new CloudSqlFieldSchema("mediumIntField", MEDIUMINT), new CloudSqlFieldSchema("charField", CHAR),
        new CloudSqlFieldSchema("varcharField", VARCHAR, 200), new CloudSqlFieldSchema("blobField", BLOB),
        new CloudSqlFieldSchema("textField", TEXT), new CloudSqlFieldSchema("tinyBlobField", TINYBLOB),
        new CloudSqlFieldSchema("tinyTextField", TINYTEXT), new CloudSqlFieldSchema("mediumBlobField", MEDIUMBLOB),
        new CloudSqlFieldSchema("mediumTextField", MEDIUMTEXT), new CloudSqlFieldSchema("longBlobField", LONGBLOB),
        new CloudSqlFieldSchema("longTextField", LONGTEXT), new CloudSqlFieldSchema("floatField", FLOAT),
        new CloudSqlFieldSchema("doubleField", DOUBLE), new CloudSqlFieldSchema("decimalField", DECIMAL),
        new CloudSqlFieldSchema("dateField", DATE), new CloudSqlFieldSchema("dateTimeField", DATETIME),
        new CloudSqlFieldSchema("timestampField", TIMESTAMP), new CloudSqlFieldSchema("timeField", TIME),
        new CloudSqlFieldSchema("yearField", YEAR)};
  }
  //CSON: MagicNumber

  private CloudSqlTableSchema givenSchema(CloudSqlFieldSchema... fields) {
    return new CloudSqlTableSchema(Arrays.asList(fields));
  }

  @Test
  public void givenSchemaWithNonNullableFieldsAndDefaults_whenSqlAdminApiCreateTableIsInvoked_thenTableIsCreated() {
    CloudSqlTableSchema cloudSqlTableSchema = givenSchema(givenNonNullableFieldsWithDefaults());

    sqlAdminService.createTable(TEST_TABLE_WITH_DEFAULTS, cloudSqlTableSchema);

    boolean tableCreated = sqlAdminService.tableExists(TEST_TABLE_WITH_DEFAULTS);
    assertThat(tableCreated, is(true));
  }

  //CSOFF: MagicNumber
  private CloudSqlFieldSchema[] givenNonNullableFieldsWithDefaults() {
    return new CloudSqlFieldSchema[] {
        new CloudSqlFieldSchema("intField", INT, 10, false, 33333), new CloudSqlFieldSchema(
        "tinyIntField", TINYINT, 4,
        false, 10), new CloudSqlFieldSchema("bigIntField", BIGINT, 18, false, 456789), new CloudSqlFieldSchema(
        "smallIntField", SMALLINT, 5, false, 1234), new CloudSqlFieldSchema(
        "mediumIntField",
        MEDIUMINT,
        9,
        false,
        123456), new CloudSqlFieldSchema(
        "charField", CHAR, 30, false, "S"), new CloudSqlFieldSchema("varcharField", VARCHAR, 255, false, "Test value"),
        new CloudSqlFieldSchema("blobField", BLOB, false), new CloudSqlFieldSchema("textField", TEXT, false),
        new CloudSqlFieldSchema("tinyBlobField", TINYBLOB, false), new CloudSqlFieldSchema(
        "tinyTextField", TINYTEXT, false), new CloudSqlFieldSchema("mediumBlobField", MEDIUMBLOB, false),
        new CloudSqlFieldSchema(
            "mediumTextField", MEDIUMTEXT, false), new CloudSqlFieldSchema("longBlobField", LONGBLOB, false),
        new CloudSqlFieldSchema(
            "longTextField", LONGTEXT, false), new CloudSqlFieldSchema("floatField", FLOAT, false, 345.6),
        new CloudSqlFieldSchema(
            "doubleField", DOUBLE, false, 6542.34454545), new CloudSqlFieldSchema(
        "decimalField",
        DECIMAL,
        false,
        6543.4343), new CloudSqlFieldSchema(
        "dateField", DATE, false), new CloudSqlFieldSchema("dateTimeField", DATETIME, false, "current_timestamp"),
        new CloudSqlFieldSchema(
            "timestampField", TIMESTAMP, false, "current_timestamp"), new CloudSqlFieldSchema("timeField", TIME, false),
        new CloudSqlFieldSchema(
            "yearField", YEAR, 2, false)};
  }
  //CSON: MagicNumber

  @Test
  public void givenSchemaWithPrimaryKey_whenSqlAdminApiCreateTableIsInvoked_thenTableIsCreated() {
    CloudSqlTableSchema cloudSqlTableSchema = givenSchemaWithPrimaryKey(
        givenPrimaryKey("intField", "floatField"), givenFields());

    sqlAdminService.createTable(TEST_TABLE_WITH_PRIMARY_KEY, cloudSqlTableSchema);

    boolean tableCreated = sqlAdminService.tableExists(TEST_TABLE_WITH_PRIMARY_KEY);
    assertThat(tableCreated, is(true));
  }

  private CloudSqlTableSchema givenSchemaWithPrimaryKey(
      CloudSqlTablePrimaryKey primaryKey, CloudSqlFieldSchema... fields) {
    return new CloudSqlTableSchema(Arrays.asList(fields), primaryKey);
  }

  private CloudSqlTablePrimaryKey givenPrimaryKey(final String... primaryKeyFieldNames) {
    List<CloudSqlFieldSchema> primaryKeyFields = Lists.newArrayList(
        Collections2.filter(
            Arrays.asList(givenFields()), new Predicate<CloudSqlFieldSchema>() {
              @Override
              public boolean apply(CloudSqlFieldSchema input) {
                for (String primaryKeyField : primaryKeyFieldNames) {
                  if (StringUtils.equals(input.getFieldName(), primaryKeyField)) {
                    return true;
                  }
                }
                return false;
              }
            }));

    return new CloudSqlTablePrimaryKey(primaryKeyFields);
  }
}
