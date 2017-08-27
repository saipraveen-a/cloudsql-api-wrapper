package com.google.cloudsql.instance.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GetTableExtractor implements Extractor<String> {
  private final ResultSet resultSet;

  public GetTableExtractor(ResultSet resultSet) {
    this.resultSet = resultSet;
  }

  @Override
  public String extractData() throws SQLException {
    if (resultSet.next()) {
      return resultSet.getString("TABLENAME");
    }
    return null;
  }
}
