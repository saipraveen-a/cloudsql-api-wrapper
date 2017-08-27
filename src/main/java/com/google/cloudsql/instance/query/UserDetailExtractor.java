package com.google.cloudsql.instance.query;

import com.google.cloudsql.instance.model.CloudSqlUser;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDetailExtractor implements Extractor<CloudSqlUser> {
  private ResultSet resultSet;

  public UserDetailExtractor(ResultSet resultSet) {
    this.resultSet = resultSet;
  }

  public CloudSqlUser extractData() throws SQLException {
    if (resultSet.next()) {
      String userName = resultSet.getString("user");
      String password = resultSet.getString("password");
      String host = resultSet.getString("host");
      CloudSqlUser user = new CloudSqlUser(userName, password, host);
      return user;
    }
    return null;
  }
}
