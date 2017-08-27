package com.google.cloudsql.instance.model;

import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.BLOB;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.INT;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.junit.Assert.assertThat;

import com.google.cloudsql.exception.CloudSqlSchemaException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;
import java.util.List;

public class CloudSqlTablePrimaryKeyTest {

  @Rule
  //CSOFF: VisibilityModifier
  public ExpectedException expectedException = ExpectedException.none();
  //CSON: VisibilityModifier

  @Test
  public void givenNullFields_whenNewCloudSqlPrimaryKey_thenThrowCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage(
        "Invalid Schema : Invalid primary key. No fields provided");

    new CloudSqlTablePrimaryKey(null);
  }

  @Test
  public void givenEmptyFields_whenNewCloudSqlPrimaryKey_thenThrowCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage("Invalid Schema : Invalid primary key. No fields provided");

    new CloudSqlTablePrimaryKey(new ArrayList<CloudSqlFieldSchema>());
  }

  @Test
  public void givenDuplicateFields_whenNewCloudSqlPrimaryKey_thenThrowCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage("Duplicate field name [field1] in [primary key]");

    new CloudSqlTablePrimaryKey(
        asList(
            new CloudSqlFieldSchema("field1", INT), new CloudSqlFieldSchema("field1", BLOB)));
  }

  @Test
  public void givenValidFields_whenNewCloudSqlPrimaryKey_thenCreatesCloudSqlPrimaryKey() {
    CloudSqlFieldSchema field1 = new CloudSqlFieldSchema("field1", INT);
    CloudSqlFieldSchema field2 = new CloudSqlFieldSchema("field2", BLOB);
    CloudSqlTablePrimaryKey cloudSqlTablePrimaryKey = new CloudSqlTablePrimaryKey(
        asList(field1, field2));

    List<CloudSqlFieldSchema> primaryKeyFields = cloudSqlTablePrimaryKey.getPrimaryKeyFields();
    assertThat(primaryKeyFields, new ReflectionEquals(unmodifiableList(asList(field1, field2))));
  }
}