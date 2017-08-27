package com.google.cloudsql.instance.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GetUserPrivilegesQuery extends Query<List<String>> {
  private static final String GET_USER_PRIVILEGES_QUERY = "show grants for ?";
  private String user;

  public GetUserPrivilegesQuery(QueryInstanceDetails queryInstanceDetails, String user) {
    super(queryInstanceDetails);
    this.user = user;
  }

  @Override
  protected List<String> executeStatement(PreparedStatement statement) throws SQLException {
    try (ResultSet resultSet = statement.executeQuery();) {
      return getExtractor(resultSet).extractData();
    }
  }

  private Extractor<List<String>> getExtractor(ResultSet resultSet) {
    return new UserPrivilegesExtractor(resultSet);
  }

  @Override
  protected void bindParameters(PreparedStatement statement) throws SQLException {
    statement.setString(1, user);
  }

  @Override
  protected String getQuery() {
    return GET_USER_PRIVILEGES_QUERY;
  }
}
