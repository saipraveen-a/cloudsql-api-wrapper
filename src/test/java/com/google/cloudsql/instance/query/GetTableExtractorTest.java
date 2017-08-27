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

public class GetTableExtractorTest {
  // CSOFF: VisibilityModifierCheck
  @Rule
  public ExpectedException resultSetFetchException = ExpectedException.none();
  // CSON: VisibilityModifierCheck

  @Test
  public void getTable_whenResultSetHasData_thenTableIsReturned() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    GetTableExtractor tableExtractor = new GetTableExtractor(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getString("TABLENAME")).thenReturn("some table");

    String database = tableExtractor.extractData();

    assertThat(database, is(equalTo("some table")));
  }

  @Test
  public void getTable_whenResultSetHasNoData_thenTableIsNull() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    GetTableExtractor tableExtractor = new GetTableExtractor(resultSet);
    when(resultSet.next()).thenReturn(false);

    String table = tableExtractor.extractData();

    assertNull(table);
  }

  @Test
  public void getTable_whenResultSetThrowsException_thenExpectedExceptionIsThrown() throws SQLException {
    resultSetFetchException.expect(SQLException.class);

    ResultSet resultSet = mock(ResultSet.class);
    GetTableExtractor tableExtractor = new GetTableExtractor(resultSet);
    when(resultSet.next()).thenThrow(new SQLException("some message"));

    tableExtractor.extractData();
  }
}

