package com.google.cloudsql.instance.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GrantUsageAndReloadPrivilegesQuery extends Query<Integer> {
  private static final String GRANT_USAGE_RELOAD_PRIVILEGES_QUERY = "GRANT USAGE, RELOAD ON *.* to ? WITH GRANT OPTION";

  private String user;

  public GrantUsageAndReloadPrivilegesQuery(QueryInstanceDetails queryInstanceDetails, String user) {
    super(queryInstanceDetails);
    this.user = user;
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
    return GRANT_USAGE_RELOAD_PRIVILEGES_QUERY;
  }
}
