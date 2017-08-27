package com.google.cloudsql.instance.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.verification.VerificationMode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class UserPrivilegesExtractorTest {
  private static final VerificationMode IS_INVOKED_THREE_TIMES = times(3);
  private static final int ZERO = 0;

  // CSOFF: VisibilityModifierCheck
  @Rule
  public ExpectedException resultSetFetchException = ExpectedException.none();
  // CSON: VisibilityModifierCheck

  @Test
  public void getUserPrivileges_whenResultSetHasData_thenPrivilegesAreReturned() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    UserPrivilegesExtractor userPrivilegesExtractor = new UserPrivilegesExtractor(resultSet);
    when(resultSet.next()).thenReturn(true, true, true, false);
    when(resultSet.getString(1)).thenReturn("grant 1", "grant 2", "grant 3");

    List<String> privileges = userPrivilegesExtractor.extractData();

    verify(resultSet, IS_INVOKED_THREE_TIMES).getString(1);
    assertThat(privileges, is(equalTo(Arrays.asList("grant 1", "grant 2", "grant 3"))));
  }

  @Test
  public void getUserPrivileges_whenResultSetHasNoData_thenPrivilegesIsEmpty() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    UserPrivilegesExtractor userPrivilegesExtractor = new UserPrivilegesExtractor(resultSet);
    when(resultSet.next()).thenReturn(false);

    List<String> privileges = userPrivilegesExtractor.extractData();

    assertThat(privileges.size(), is(equalTo(ZERO)));
  }

  @Test
  public void getUserPrivileges_whenResultSetThrowsException_thenExpectedExceptionIsThrown() throws SQLException {
    resultSetFetchException.expect(SQLException.class);

    ResultSet resultSet = mock(ResultSet.class);
    UserPrivilegesExtractor userPrivilegesExtractor = new UserPrivilegesExtractor(resultSet);
    when(resultSet.next()).thenThrow(new SQLException("some message"));

    userPrivilegesExtractor.extractData();
  }
}
