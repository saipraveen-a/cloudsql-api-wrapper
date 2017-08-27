package com.google.cloudsql.query;

import com.google.cloudsql.instance.model.CloudSqlTable;
import com.google.cloudsql.instance.query.Query;
import com.google.cloudsql.instance.query.QueryInstanceDetails;

import com.google.common.collect.Lists;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SelectTestUserQuery extends Query<List<TestTableRow>> {

  private final String selectTestTable;

  public SelectTestUserQuery(QueryInstanceDetails queryInstanceDetails, String database, CloudSqlTable table) {
    super(queryInstanceDetails);
    selectTestTable = String.format("select * from  %s.%s", database, table.getName());
  }

  @Override
  protected List<TestTableRow> executeStatement(PreparedStatement statement) throws SQLException {
    List<TestTableRow> testTableRows = Lists.newLinkedList();
    ResultSet resultSet = null;
    try {
      resultSet = statement.executeQuery();
      while (resultSet.next()) {
        final int id = resultSet.getInt(1);
        final String name = resultSet.getString(2);
        testTableRows.add(new TestTableRow(id, name));
      }
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
    }

    return testTableRows;
  }

  @Override
  protected void bindParameters(PreparedStatement statement) throws SQLException {

  }

  @Override
  protected String getQuery() {
    return selectTestTable;
  }
}
