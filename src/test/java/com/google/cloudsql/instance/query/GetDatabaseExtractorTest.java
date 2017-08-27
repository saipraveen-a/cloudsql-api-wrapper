package com.google.cloudsql.instance.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GetDatabaseExtractorTest {
  // CSOFF: VisibilityModifierCheck
  @Rule
  public ExpectedException resultSetFetchException = ExpectedException.none();
  // CSON: VisibilityModifierCheck

  @Test
  public void getDatabase_whenResultSetHasData_thenDatabaseIsReturned() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    GetDatabaseExtractor databaseExtractor = new GetDatabaseExtractor(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getString("db")).thenReturn("some database");

    String database = databaseExtractor.extractData();

    assertThat(database, is(equalTo("some database")));
  }

  @Test
  public void getDatabase_whenResultSetHasNoData_thenDatabaseIsNull() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    GetDatabaseExtractor databaseExtractor = new GetDatabaseExtractor(resultSet);
    when(resultSet.next()).thenReturn(false);

    String database = databaseExtractor.extractData();

    assertNull(database);
  }

  @Test
  public void getDatabase_whenResultSetThrowsException_thenExpectedExceptionIsThrown() throws SQLException {
    resultSetFetchException.expect(SQLException.class);

    ResultSet resultSet = mock(ResultSet.class);
    GetDatabaseExtractor databaseExtractor = new GetDatabaseExtractor(resultSet);
    when(resultSet.next()).thenThrow(new SQLException("some message"));

    databaseExtractor.extractData();
  }
}
