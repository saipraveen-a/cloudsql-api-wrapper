package com.google.cloudsql.instance.query;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.join;

import com.google.cloudsql.instance.model.CloudSqlDatabase;
import com.google.cloudsql.instance.model.CloudSqlFieldSchema;
import com.google.cloudsql.instance.model.CloudSqlFieldSchema.DataType;
import com.google.cloudsql.instance.model.CloudSqlTable;
import com.google.cloudsql.instance.model.CloudSqlTablePrimaryKey;
import com.google.cloudsql.instance.model.CloudSqlTableSchema;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateTableQuery extends Query<Integer> {

  private CloudSqlDatabase cloudSqlDatabase;
  private CloudSqlTable cloudSqlTable;
  private CloudSqlTableSchema cloudSqlTableSchema;

  public CreateTableQuery(
      QueryInstanceDetails queryInstanceDetails,
      CloudSqlDatabase cloudSqlDatabase,
      CloudSqlTable cloudSqlTable,
      CloudSqlTableSchema cloudSqlTableSchema) {
    super(queryInstanceDetails);
    this.cloudSqlDatabase = cloudSqlDatabase;
    this.cloudSqlTable = cloudSqlTable;
    this.cloudSqlTableSchema = cloudSqlTableSchema;
  }

  @Override
  protected Integer executeStatement(PreparedStatement statement) throws SQLException {
    return statement.executeUpdate();
  }

  @Override
  protected void bindParameters(PreparedStatement statement) throws SQLException {

  }

  @Override
  protected String getQuery() {
    String database = cloudSqlDatabase.getName();
    String table = cloudSqlTable.getName();

    return String.format(
        "CREATE TABLE %s.%s (%s%s)", database, table, getCreateFieldsStatement(
            cloudSqlTableSchema.getFields()), getPrimaryKeyStatement(cloudSqlTableSchema.getPrimaryKey()));
  }

  private String getCreateFieldsStatement(List<CloudSqlFieldSchema> fields) {
    List<String> createFieldSchemaList = new ArrayList<>();
    for (CloudSqlFieldSchema fieldSchema : fields) {
      createFieldSchemaList.add(getCreateFieldStatement(fieldSchema));
    }

    return collateFields(createFieldSchemaList);
  }

  private String collateFields(List<String> fields) {
    return join(fields, ", ");
  }

  private String getCreateFieldStatement(CloudSqlFieldSchema cloudSqlFieldSchema) {
    StringBuilder field = new StringBuilder();

    String fieldName = cloudSqlFieldSchema.getFieldName();
    DataType dataType = cloudSqlFieldSchema.getDataType();
    int fieldWidth = cloudSqlFieldSchema.getWidth();
    Object defaultValue = cloudSqlFieldSchema.getDefaultValue();

    field.append(fieldName).append(SPACE).append(dataType.name());

    if (fieldWidth > 0) {
      field.append(getWidthStatement(fieldWidth));
    }
    if (dataType.isDefaultValueSupported() && defaultValue != null) {
      field.append(getDefaultValueStatement(cloudSqlFieldSchema));
    }
    if (!cloudSqlFieldSchema.isNullable()) {
      field.append(getNotNullStatement());
    }
    return field.toString();
  }

  private String getWidthStatement(int width) {
    return String.format("(%s)", width);
  }

  private String getDefaultValueStatement(
      CloudSqlFieldSchema fieldSchema) {
    return String.format(" DEFAULT %s", getDefaultValueString(fieldSchema));
  }

  private String getDefaultValueString(CloudSqlFieldSchema field) {
    if (field.isNullDefaultValue()) {
      return "NULL";
    }
    if (field.getDataType().isTextType()) {
      return String.format("'%s'", field.getDefaultValue());
    }
    return String.valueOf(field.getDefaultValue());
  }

  private String getNotNullStatement() {
    return " NOT NULL";
  }

  private String getPrimaryKeyStatement(CloudSqlTablePrimaryKey primaryKey) {
    String primaryKeyStatement = EMPTY;
    if (primaryKey != null && primaryKey.getPrimaryKeyFields() != null && !primaryKey.getPrimaryKeyFields().isEmpty()) {
      List<String> primaryKeyFieldSchemaList = new ArrayList<>();
      for (CloudSqlFieldSchema cloudSqlFieldSchema : primaryKey.getPrimaryKeyFields()) {
        primaryKeyFieldSchemaList.add(cloudSqlFieldSchema.getFieldName());
      }

      primaryKeyStatement = String.format(
          ", PRIMARY KEY (%s)", collateFields(primaryKeyFieldSchemaList));
    }

    return primaryKeyStatement;
  }
}
