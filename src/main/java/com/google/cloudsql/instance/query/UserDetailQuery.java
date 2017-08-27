package com.google.cloudsql.instance.query;

import com.google.cloudsql.instance.model.CloudSqlUser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDetailQuery extends Query<CloudSqlUser> {

  private static final String GET_USER_QUERY = "select user, password, host from mysql.user where user = ?";

  private String user;

  public UserDetailQuery(QueryInstanceDetails queryInstanceDetails, String user) {
    super(queryInstanceDetails);
    this.user = user;
  }

  @Override
  protected CloudSqlUser executeStatement(PreparedStatement statement) throws SQLException {
    try (ResultSet resultSet = statement.executeQuery();) {
      return getExtractor(resultSet).extractData();
    }
  }

  protected Extractor<CloudSqlUser> getExtractor(ResultSet resultSet) {
    return new UserDetailExtractor(resultSet);
  }

  @Override
  protected void bindParameters(PreparedStatement statement) throws SQLException {
    statement.setString(1, user);
  }

  @Override
  protected String getQuery() {
    return GET_USER_QUERY;
  }
}
