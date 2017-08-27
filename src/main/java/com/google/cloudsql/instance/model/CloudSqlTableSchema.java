package com.google.cloudsql.instance.model;

import static com.google.cloudsql.instance.util.CloudSqlSchemaUtils.checkDuplicatesAndGetFieldNames;
import static com.google.cloudsql.instance.util.CloudSqlSchemaUtils.verify;
import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.containsAll;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Set;

// TODO: Add foreign key support.
public class CloudSqlTableSchema {

  private final List<CloudSqlFieldSchema> fields;
  private final CloudSqlTablePrimaryKey primaryKey;

  public CloudSqlTableSchema(List<CloudSqlFieldSchema> fields) {
    this(fields, null);
  }

  public CloudSqlTableSchema(List<CloudSqlFieldSchema> fields, CloudSqlTablePrimaryKey primaryKey) {
    this.fields = fields;
    this.primaryKey = primaryKey;

    Set<String> fieldNames = verifyAndGetUniqueFieldNames();
    verifyPrimaryKeyWithColumnNames(fieldNames);
  }

  private Set<String> verifyAndGetUniqueFieldNames() {
    verify(isEmpty(fields), "No columns provided. At least one column is required to create a table");

    return checkDuplicatesAndGetFieldNames(fields, "columns");
  }

  private void verifyPrimaryKeyWithColumnNames(Set<String> fieldNames) {
    if (primaryKey != null) {
      Set<String> primaryKeyFieldNames =
          checkDuplicatesAndGetFieldNames(primaryKey.getPrimaryKeyFields(), "primary key");

      verify(
          !containsAll(fieldNames, primaryKeyFieldNames),
          format("Missing primary key fields in columns, key fields %s, columns %s", primaryKeyFieldNames, fieldNames));
    }
  }

  public List<CloudSqlFieldSchema> getFields() {
    return Collections.unmodifiableList(fields);
  }

  public CloudSqlTablePrimaryKey getPrimaryKey() {
    return primaryKey;
  }
}
