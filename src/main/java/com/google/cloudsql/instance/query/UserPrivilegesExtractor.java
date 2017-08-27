package com.google.cloudsql.instance.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserPrivilegesExtractor implements Extractor<List<String>> {
  private ResultSet resultSet;

  public UserPrivilegesExtractor(ResultSet resultSet) {
    this.resultSet = resultSet;
  }

  @Override
  public List<String> extractData() throws SQLException {
    List<String> privileges = new ArrayList<String>();

    while (resultSet.next()) {
      privileges.add(resultSet.getString(1));
    }
    return privileges;
  }
}
