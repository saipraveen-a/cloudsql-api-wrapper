package com.google.cloudsql.instance.util;

import static java.lang.String.format;

import com.google.cloudsql.exception.CloudSqlSchemaException;
import com.google.cloudsql.instance.model.CloudSqlFieldSchema;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CloudSqlSchemaUtils {

  private CloudSqlSchemaUtils() {
  }

  public static void verify(boolean failureCondition, String failureMessage) {
    if (failureCondition) {
      throw new CloudSqlSchemaException("Invalid Schema : " + failureMessage);
    }
  }

  public static Set<String> checkDuplicatesAndGetFieldNames(List<CloudSqlFieldSchema> fields, String fieldSource) {
    Set<String> fieldNames = new LinkedHashSet<>();
    if (fields != null) {
      for (CloudSqlFieldSchema fieldSchema : fields) {
        String fieldName = fieldSchema.getFieldName();
        verify(
            fieldNames.contains(fieldName), format(
                "Duplicate field name [%s] in [%s]", fieldName, fieldSource));
        fieldNames.add(fieldSchema.getFieldName());
      }
    }
    return fieldNames;
  }

  public static void checkDuplicateFields(List<CloudSqlFieldSchema> fields, String fieldSource) {
    Set<String> fieldNames = new LinkedHashSet<>();
    if (fields != null) {
      for (CloudSqlFieldSchema fieldSchema : fields) {
        String fieldName = fieldSchema.getFieldName();
        verify(
            fieldNames.contains(fieldName), format(
                "Duplicate field name [%s] in [%s]", fieldName, fieldSource));
        fieldNames.add(fieldSchema.getFieldName());
      }
    }
  }
}
