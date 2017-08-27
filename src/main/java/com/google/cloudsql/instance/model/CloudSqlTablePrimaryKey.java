package com.google.cloudsql.instance.model;

import static com.google.cloudsql.instance.util.CloudSqlSchemaUtils.checkDuplicateFields;
import static com.google.cloudsql.instance.util.CloudSqlSchemaUtils.verify;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.Collections;
import java.util.List;

public class CloudSqlTablePrimaryKey {

  private List<CloudSqlFieldSchema> primaryKeyFields;

  public CloudSqlTablePrimaryKey(List<CloudSqlFieldSchema> primaryKeyFields) {
    this.primaryKeyFields = primaryKeyFields;

    verify(
        isEmpty(primaryKeyFields), "Invalid primary key. No fields provided");

    checkDuplicateFields(primaryKeyFields, "primary key");
  }

  public List<CloudSqlFieldSchema> getPrimaryKeyFields() {
    return Collections.unmodifiableList(primaryKeyFields);
  }
}
