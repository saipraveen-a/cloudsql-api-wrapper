package com.google.cloudsql.instance.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateDatabaseQuery extends Query<Integer> {
  private static final String CREATE_DATABASE_QUERY = "CREATE DATABASE ";
  private String databaseName;

  public CreateDatabaseQuery(QueryInstanceDetails queryInstanceDetails, String databaseName) {
    super(queryInstanceDetails);
    this.databaseName = databaseName;
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
    return CREATE_DATABASE_QUERY + databaseName;
  }
}
