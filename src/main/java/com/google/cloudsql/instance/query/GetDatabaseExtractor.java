package com.google.cloudsql.instance.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GetDatabaseExtractor implements Extractor<String> {
  private ResultSet resultSet;

  public GetDatabaseExtractor(ResultSet resultSet) {
    this.resultSet = resultSet;
  }

  @Override
  public String extractData() throws SQLException {
    if (resultSet.next()) {
      return resultSet.getString("db");
    }
    return null;
  }
}
