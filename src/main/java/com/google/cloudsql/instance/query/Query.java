package com.google.cloudsql.instance.query;

import com.google.cloudsql.exception.CloudSqlException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Query<T> {
  private static final String MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
  private static final String CLOUD_SQL_ROOT_USER_NAME = "root";
  private static final int QUERY_TIMEOUT_IN_SECONDS = 60;

  private String rootPassword;
  private String url;

  public Query(QueryInstanceDetails queryInstanceDetails) {
    this.rootPassword = queryInstanceDetails.getRootPassword();
    this.url = String.format(
        "jdbc:mysql://google?cloudSqlInstance=%s"
            + ":us-central1:%s&socketFactory=com.google.cloud.sql.mysql.SocketFactory",
        queryInstanceDetails.getProjectId(),
        queryInstanceDetails.getInstanceName());
  }

  public T executeQuery() throws SQLException {
    loadDriver();

    try (Connection connection = DriverManager.getConnection(url, CLOUD_SQL_ROOT_USER_NAME, rootPassword);
         PreparedStatement statement = connection.prepareStatement(getQuery())) {
      bindParameters(statement);
      statement.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS);
      return executeStatement(statement);
    }
  }

  private void loadDriver() {
    try {
      Class.forName(MYSQL_JDBC_DRIVER);
    } catch (ClassNotFoundException e) {
      throw new CloudSqlException("Could not load Jdbc driver", e);
    }
  }

  protected abstract T executeStatement(PreparedStatement statement) throws SQLException;

  protected abstract void bindParameters(PreparedStatement statement) throws SQLException;

  protected abstract String getQuery();
}
