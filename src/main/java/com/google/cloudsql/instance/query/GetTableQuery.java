package com.google.cloudsql.instance.query;

import com.google.cloudsql.instance.model.CloudSqlDatabase;
import com.google.cloudsql.instance.model.CloudSqlTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetTableQuery extends Query<String> {
  private static final String GET_TABLE_QUERY = "SELECT TABLE_NAME AS TABLENAME FROM INFORMATION_SCHEMA.TABLES "
      + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";

  private final CloudSqlDatabase database;
  private final CloudSqlTable table;

  public GetTableQuery(
      QueryInstanceDetails queryInstanceDetails,
      CloudSqlDatabase database,
      CloudSqlTable table) {
    super(queryInstanceDetails);
    this.database = database;
    this.table = table;
  }

  @Override
  protected String executeStatement(PreparedStatement statement) throws SQLException {
    try (ResultSet resultSet = statement.executeQuery();) {
      return getExtractor(resultSet).extractData();
    }
  }

  protected Extractor<String> getExtractor(ResultSet resultSet) {
    return new GetTableExtractor(resultSet);
  }


  @Override
  protected void bindParameters(PreparedStatement statement) throws SQLException {
    statement.setString(1, database.getName());
    statement.setString(2, table.getName());
  }

  @Override
  protected String getQuery() {
    return GET_TABLE_QUERY;
  }
}
