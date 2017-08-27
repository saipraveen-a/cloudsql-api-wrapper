package com.google.cloudsql.instance.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GrantAllPrivilegesQuery extends Query<Integer> {
  private static final String GRANT_ALL_PRIVILEGES_QUERY = "GRANT ALL ON %s.* to ?";
  private final String user;
  private final String databaseName;

  public GrantAllPrivilegesQuery(QueryInstanceDetails queryInstanceDetails, String user,
                                 String databaseName) {
    super(queryInstanceDetails);
    this.user = user;
    this.databaseName = databaseName;
  }

  @Override
  protected Integer executeStatement(PreparedStatement statement) throws SQLException {
    return statement.executeUpdate();
  }

  @Override
  protected void bindParameters(PreparedStatement statement) throws SQLException {
    statement.setString(1, user);
  }

  @Override
  protected String getQuery() {
    return String.format(GRANT_ALL_PRIVILEGES_QUERY, databaseName);
  }
}
