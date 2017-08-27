package com.google.cloudsql.instance.model;

import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.BLOB;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.INT;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.VARCHAR;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType.YEAR;
import static com.google.cloudsql.instance.model.CloudSqlFieldSchema.DefaultValues.NULL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.cloudsql.exception.CloudSqlSchemaException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CloudSqlFieldSchemaTest {

  private static final int ANY_WIDTH = 0;
  private static final int INT_WIDTH_OVER_LIMIT = 20;
  private static final int INT_MAX_WIDTH = 11;
  private static final int YEAR_WIDTH_4 = 4;
  private static final int YEAR_WIDTH_2 = 2;

  @Rule
  //CSOFF: VisibilityModifier
  public ExpectedException expectedException = ExpectedException.none();
  //CSON: VisibilityModifier

  @Test
  public void givenDataTypeWithWidthRequiredAndNoWidth_whenCloudSqlFieldSchema_thenThrowsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage("Invalid Schema : Field [widthMandatoryField], Width must be in range [1-255]");

    new CloudSqlFieldSchema("widthMandatoryField", VARCHAR);
  }

  @Test
  public void givenDataTypeWithWidthNotMandatoryAndNoWidth_whenCloudSqlFieldSchema_thenCreatesFieldSchema() {
    CloudSqlFieldSchema widthNotMandatoryField = new CloudSqlFieldSchema("widthNotMandatoryField", INT);

    assertThat(widthNotMandatoryField.getWidth(), is(ANY_WIDTH));
  }

  @Test
  public void givenDataTypeWithNoWithSupportAndSomeWidth_whenCloudSqlFieldSchema_thenThrowsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage("Invalid Schema : Field [fieldWithNoWithSupport], Width not supported");

    new CloudSqlFieldSchema("fieldWithNoWithSupport", BLOB, 1);
  }

  @Test
  public void givenDataTypeWithNoWithSupportAndNoWidth_whenCloudSqlFieldSchema_thenCreatesFieldSchema() {
    CloudSqlFieldSchema fieldWithNoWithSupport = new CloudSqlFieldSchema("fieldWithNoWithSupport", BLOB);

    assertThat(fieldWithNoWithSupport, is(notNullValue()));
  }

  @Test
  public void givenDataTypeWithWidthOverflow_whenCloudSqlFieldSchema_thenThrowsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage("Invalid Schema : Field [intField], Width must be in range [0-11]");

    new CloudSqlFieldSchema("intField", INT, INT_WIDTH_OVER_LIMIT);
  }

  @Test
  public void givenDataTypeWithWidthInLimits_whenCloudSqlFieldSchema_thenCreatesFieldSchema() {
    CloudSqlFieldSchema intField = new CloudSqlFieldSchema("intField", INT, INT_MAX_WIDTH);

    assertThat(intField, is(notNullValue()));
    assertThat(intField.getWidth(), is(INT_MAX_WIDTH));
  }

  @Test
  public void givenDataTypeForYearFieldAndInvalidWidth_whenCloudSqlFieldSchema_thenThrowsCloudSqlSchemaException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage("Invalid Schema : Field [yearField], Allowed values for width [0, 2, 4]");

    new CloudSqlFieldSchema("yearField", YEAR, 1);
  }

  @Test
  public void givenDataTypeForYearFieldWithDefaultWidth_whenCloudSqlFieldSchema_thenCreatesFieldSchema() {
    CloudSqlFieldSchema yearField = new CloudSqlFieldSchema("yearField", YEAR);

    assertThat(yearField, is(notNullValue()));
  }

  @Test
  public void givenDataTypeForYearFieldWithWidth2_whenCloudSqlFieldSchema_thenCreatesFieldSchema() {
    CloudSqlFieldSchema yearField = new CloudSqlFieldSchema("yearField", YEAR, YEAR_WIDTH_2);

    assertThat(yearField, is(notNullValue()));
  }

  @Test
  public void givenDataTypeForYearFieldWithWidth4_whenCloudSqlFieldSchema_thenCreatesFieldSchema() {
    CloudSqlFieldSchema yearField = new CloudSqlFieldSchema("yearField", YEAR, YEAR_WIDTH_4);

    assertThat(yearField, is(notNullValue()));
  }

  @Test
  public void givenDataTypeWithNoDefaultValueSupportAndDefaultValue_whenCloudSqlFieldSchema_thenThrowsException() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage(
        "Invalid Schema : Default value is not supported for field [fieldWithNoDefaultValueSupport]");

    new CloudSqlFieldSchema("fieldWithNoDefaultValueSupport", BLOB, true, "abc");
  }

  @Test
  public void givenDataTypeWithNoDefaultValueSupport_whenCloudSqlFieldSchema_thenCreatesFieldSchema() {
    CloudSqlFieldSchema fieldWithNoDefaultValueSupport = new CloudSqlFieldSchema(
        "fieldWithNoDefaultValueSupport", BLOB, true, null);

    assertThat(BLOB.isDefaultValueSupported(), is(false));
    assertThat(fieldWithNoDefaultValueSupport, is(notNullValue()));
    assertThat(fieldWithNoDefaultValueSupport.getDefaultValue(), is(nullValue()));
  }

  @Test
  public void givenNonNullableFieldWithDefaultNullValue_whenCloudSqlFieldSchema_thenCreatesFieldSchema() {
    expectedException.expect(CloudSqlSchemaException.class);
    expectedException.expectMessage(
        "Default value is null for non-nullable field [field]");

    new CloudSqlFieldSchema("field", INT, false, NULL);
  }
}