package com.google.cloudsql.instance.model;

import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.BIGINT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.DATE;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.INT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.VARCHAR;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.cloudsql.exception.CloudSqlSchemaException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;

public class CloudSqlTableSchemaTest {

  private static final int VARCHAR_FIELD_WIDTH = 100;
  @Rule
  //CSOFF: VisibilityModifier
  public ExpectedException expectedException = ExpectedException.none();
  //CSON: VisibilityModifier

  @Test
  public void givenNullFields_whenNewCloudSqlTableSchema_throwsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage(
        "Invalid Schema : No columns provided. At least one column is required to create a table");

    new CloudSqlTableSchema(null);
  }

  @Test
  public void givenEmptyFields_whenNewCloudSqlTableSchema_throwsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage(
        "Invalid Schema : No columns provided. At least one column is required to create a table");

    new CloudSqlTableSchema(new ArrayList<CloudSqlFieldSchema>());
  }

  @Test
  public void givenDuplicateFields_whenNewCloudSqlTableSchema_throwsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage(
        "Invalid Schema : Duplicate field name [field1] in [columns]");

    CloudSqlFieldSchema field1 = new CloudSqlFieldSchema("field1", INT);
    CloudSqlFieldSchema duplicateField = new CloudSqlFieldSchema("field1", BIGINT);
    CloudSqlFieldSchema field2 = new CloudSqlFieldSchema("field3", VARCHAR, VARCHAR_FIELD_WIDTH);

    new CloudSqlTableSchema(asList(field1, field2, duplicateField));
  }

  @Test
  public void givenValidFieldsAndNullPrimaryKeyFields_whenNewCloudSqlTableSchema_throwsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage(
        "Invalid Schema : Invalid primary key. No fields provided");

    new CloudSqlTableSchema(singletonList(new CloudSqlFieldSchema("field1", INT)), new CloudSqlTablePrimaryKey(null));
  }

  @Test
  public void givenValidFieldsAndEmptyPrimaryKeyFields_whenNewCloudSqlTableSchema_throwsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage(
        "Invalid Schema : Invalid primary key. No fields provided");

    new CloudSqlTableSchema(
        singletonList(
            new CloudSqlFieldSchema("field1", INT)), new CloudSqlTablePrimaryKey(new ArrayList<CloudSqlFieldSchema>()));
  }

  @Test
  public void givenValidFieldsAndInvalidPrimaryKeyFields_whenNewCloudSqlTableSchema_throwsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage(
        "Missing primary key fields in columns, key fields [field1, invalidField], columns [field1, field2, field3]");

    CloudSqlFieldSchema primaryKeyField1 = new CloudSqlFieldSchema("field1", INT);
    CloudSqlFieldSchema primaryKeyField2 = new CloudSqlFieldSchema("field2", BIGINT);

    new CloudSqlTableSchema(
        asList(
            primaryKeyField1, primaryKeyField2, new CloudSqlFieldSchema("field3", VARCHAR, VARCHAR_FIELD_WIDTH)),
        new CloudSqlTablePrimaryKey(
            asList(primaryKeyField1, new CloudSqlFieldSchema("invalidField", DATE))));
  }

  @Test
  public void givenValidFieldsAndDuplicatePrimaryKeyFields_whenNewCloudSqlTableSchema_throwsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage(
        "Invalid Schema : Duplicate field name [field1] in [primary key]");

    CloudSqlFieldSchema primaryKeyField1 = new CloudSqlFieldSchema("field1", INT);
    CloudSqlFieldSchema primaryKeyField2 = new CloudSqlFieldSchema("field2", BIGINT);

    new CloudSqlTableSchema(
        asList(
            primaryKeyField1, primaryKeyField2, new CloudSqlFieldSchema(
                "field3", VARCHAR, VARCHAR_FIELD_WIDTH)), new CloudSqlTablePrimaryKey(
        asList(primaryKeyField1, primaryKeyField1)));
  }

  @Test
  public void givenValidFieldsAndValidPrimaryKeyFields_whenNewCloudSqlTableSchema_thenCreateCloudSqlTableSchema() {
    CloudSqlFieldSchema primaryKeyField1 = new CloudSqlFieldSchema("field1", INT);
    CloudSqlFieldSchema primaryKeyField2 = new CloudSqlFieldSchema("field2", BIGINT);
    CloudSqlFieldSchema field3 = new CloudSqlFieldSchema(
        "field3", VARCHAR, VARCHAR_FIELD_WIDTH);

    CloudSqlTableSchema cloudSqlTableSchema = new CloudSqlTableSchema(
        asList(primaryKeyField1, primaryKeyField2, field3), new CloudSqlTablePrimaryKey(
        asList(
            primaryKeyField1, primaryKeyField2)));

    assertThat(cloudSqlTableSchema, is(notNullValue()));
    assertThat(
        cloudSqlTableSchema.getFields(), new ReflectionEquals(
            unmodifiableList(asList(primaryKeyField1, primaryKeyField2, field3))));
    assertThat(
        cloudSqlTableSchema.getPrimaryKey(), new ReflectionEquals(
            new CloudSqlTablePrimaryKey(asList(primaryKeyField1, primaryKeyField2))));
  }
}