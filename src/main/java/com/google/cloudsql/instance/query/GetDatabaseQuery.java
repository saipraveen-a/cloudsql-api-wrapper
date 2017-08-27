package com.google.cloudsql.instance.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetDatabaseQuery extends Query<String> {
  private static final String GET_DATABASE_QUERY = "SELECT SCHEMA_NAME AS DB FROM INFORMATION_SCHEMA.SCHEMATA "
      + "WHERE SCHEMA_NAME = ?";

  private String name;

  public GetDatabaseQuery(QueryInstanceDetails queryInstanceDetails, String name) {
    super(queryInstanceDetails);
    this.name = name;
  }

  @Override
  protected String executeStatement(PreparedStatement statement) throws SQLException {
    try (ResultSet resultSet = statement.executeQuery();) {
      return getExtractor(resultSet).extractData();
    }
  }

  protected Extractor<String> getExtractor(ResultSet resultSet) {
    return new GetDatabaseExtractor(resultSet);
  }

  @Override
  protected void bindParameters(PreparedStatement statement) throws SQLException {
    statement.setString(1, name);
  }

  @Override
  protected String getQuery() {
    return GET_DATABASE_QUERY;
  }
}
