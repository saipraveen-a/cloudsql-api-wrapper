package com.google.cloudsql.instance.model;

import static com.google.cloudsql.instance.util.CloudSqlSchemaUtils.verify;
import static java.lang.String.format;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class CloudSqlFieldSchema {

  private static final int NO_WIDTH = 0;
  private static final boolean SUPPORTS_DEFAULT_VALUES = true;

  private final String fieldName;
  private final DataType dataType;
  private final int width;
  private final Object defaultValue;
  private final boolean isNullable;

  public CloudSqlFieldSchema(String fieldName, DataType dataType) {
    this(fieldName, dataType, NO_WIDTH, true, null);
  }

  public CloudSqlFieldSchema(String fieldName, DataType dataType, int width) {
    this(fieldName, dataType, width, true, null);
  }

  public CloudSqlFieldSchema(String fieldName, DataType dataType, boolean isNullable) {
    this(fieldName, dataType, NO_WIDTH, isNullable, null);
  }

  public CloudSqlFieldSchema(String fieldName, DataType dataType, int width, boolean isNullable) {
    this(fieldName, dataType, width, isNullable, null);
  }

  public CloudSqlFieldSchema(
      String fieldName, DataType dataType, boolean isNullable, Object defaultValue) {
    this(fieldName, dataType, NO_WIDTH, isNullable, defaultValue);
  }

  public CloudSqlFieldSchema(
      String fieldName, DataType dataType, int width, boolean isNullable, Object defaultValue) {

    this.fieldName = fieldName;
    this.dataType = dataType;
    this.width = width;
    this.defaultValue = defaultValue;
    this.isNullable = isNullable;

    verifyFieldWidth();
    verifyFieldDefaultValue();
  }

  private void verifyFieldWidth() {
    verify(
        !dataType.getWidthSpecification().isValidWidth(width), format(
            "Field [%s], %s", fieldName, dataType.getWidthSpecification().getInvalidWidthMessage()));
  }

  private void verifyFieldDefaultValue() {
    verify(
        !dataType.isDefaultValueSupported() && defaultValue != null,
        "Default value is not supported for field [" + fieldName + "]");
    verify(
        dataType.isDefaultValueSupported() && !isNullable && isNullDefaultValue(), format(
            "Default value is null for non-nullable field [%s]", fieldName));
  }

  public String getFieldName() {
    return fieldName;
  }

  public DataType getDataType() {
    return dataType;
  }

  public int getWidth() {
    return width;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public boolean isNullable() {
    return isNullable;
  }

  public boolean isNullDefaultValue() {
    return defaultValue instanceof NullValue;
  }

  public static class DefaultValues {
    public static final Object NULL = new NullValue();
  }

  private static class NullValue {}

  public enum DataType {
    INT(widthRange(0, 11), SUPPORTS_DEFAULT_VALUES),
    TINYINT(widthRange(0, 4), SUPPORTS_DEFAULT_VALUES),
    SMALLINT(widthRange(0, 5), SUPPORTS_DEFAULT_VALUES),
    MEDIUMINT(widthRange(0, 9), SUPPORTS_DEFAULT_VALUES),
    BIGINT(widthRange(0, 20), SUPPORTS_DEFAULT_VALUES),
    FLOAT(SUPPORTS_DEFAULT_VALUES),
    DOUBLE(SUPPORTS_DEFAULT_VALUES),
    DECIMAL(SUPPORTS_DEFAULT_VALUES),
    DATE,
    DATETIME(SUPPORTS_DEFAULT_VALUES),
    TIMESTAMP(SUPPORTS_DEFAULT_VALUES),
    TIME,
    YEAR(widthValues(NO_WIDTH, 2, 4)),
    CHAR(widthRange(0, 255), SUPPORTS_DEFAULT_VALUES),
    VARCHAR(widthRange(1, 255), SUPPORTS_DEFAULT_VALUES),
    BLOB,
    TEXT,
    TINYBLOB,
    TINYTEXT,
    MEDIUMBLOB,
    MEDIUMTEXT,
    LONGBLOB,
    LONGTEXT;

    private final WidthSpecification widthSpecification;
    private final boolean defaultValueSupported;

    DataType() {
      this(noWidth(), false);
    }

    DataType(boolean defaultValueSupported) {
      this(noWidth(), defaultValueSupported);
    }

    DataType(WidthSpecification widthSpecification) {
      this(widthSpecification, false);
    }

    DataType(WidthSpecification widthSpecification, boolean defaultValueSupported) {
      this.widthSpecification = widthSpecification;
      this.defaultValueSupported = defaultValueSupported;
    }

    public WidthSpecification getWidthSpecification() {
      return widthSpecification;
    }

    public boolean isDefaultValueSupported() {
      return defaultValueSupported;
    }

    public boolean isTextType() {
      return equals(CHAR) || equals(VARCHAR) || equals(TEXT) || equals(TINYTEXT) || equals(MEDIUMTEXT) || equals(
          LONGTEXT);
    }

    private static WidthSpecification noWidth() {
      return new NoWidth();
    }

    private static WidthSpecification widthRange(int minimum, int maximum) {
      return new WidthRange(minimum, maximum);
    }

    private static WidthSpecification widthValues(int... values) {
      return new WidthValues(values);
    }
  }

  private interface WidthSpecification {

    boolean isValidWidth(int width);

    String getInvalidWidthMessage();
  }

  private static class NoWidth implements WidthSpecification {

    @Override
    public boolean isValidWidth(int width) {
      return width == NO_WIDTH;
    }

    @Override
    public String getInvalidWidthMessage() {
      return "Width not supported";
    }
  }

  private static class WidthRange implements WidthSpecification {
    private int minimum;
    private int maximum;

    WidthRange(int minimum, int maximum) {
      this.minimum = minimum;
      this.maximum = maximum;
    }

    @Override
    public boolean isValidWidth(int width) {
      return width >= minimum && width <= maximum;
    }

    @Override
    public String getInvalidWidthMessage() {
      return format("Width must be in range [%s-%s]", minimum, maximum);
    }
  }

  private static class WidthValues implements WidthSpecification {
    private Set<Integer> widthValues = new LinkedHashSet<>();

    WidthValues(int... widthValues) {
      this.widthValues = new HashSet<>();
      for (int value : widthValues) {
        this.widthValues.add(value);
      }
    }

    @Override
    public boolean isValidWidth(int width) {
      return widthValues.contains(width);
    }

    @Override
    public String getInvalidWidthMessage() {
      return format("Allowed values for width %s", widthValues);
    }
  }
}


