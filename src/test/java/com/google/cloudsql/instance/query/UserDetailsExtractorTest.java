package com.google.cloudsql.instance.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloudsql.instance.model.CloudSqlUser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDetailsExtractorTest {
  // CSOFF: VisibilityModifierCheck
  @Rule
  public ExpectedException resultSetFetchException = ExpectedException.none();
  // CSON: VisibilityModifierCheck

  @Test
  public void getUserDetails_whenResultSetHasData_thenUserDetailsAreReturned() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    UserDetailExtractor userDetailExtractor = new UserDetailExtractor(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getString("user")).thenReturn("some user");
    when(resultSet.getString("password")).thenReturn("some password");
    when(resultSet.getString("host")).thenReturn("some host");

    CloudSqlUser user = userDetailExtractor.extractData();

    assertThat(user.getUser(), is(equalTo("some user")));
    assertThat(user.getPassword(), is(equalTo("some password")));
    assertThat(user.getHost(), is(equalTo("some host")));
  }

  @Test
  public void getUserDetails_whenResultSetHasNoData_thenUserDetailsShoudBeNull() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    UserDetailExtractor userDetailExtractor = new UserDetailExtractor(resultSet);
    when(resultSet.next()).thenReturn(false);

    CloudSqlUser user = userDetailExtractor.extractData();

    assertNull(user);
  }

  @Test
  public void getUserDetails_whenExceptionIsThrownWhileFetchingData_thenExpectedExceptionIsThrown()
      throws SQLException {
    resultSetFetchException.expect(SQLException.class);
    ResultSet resultSet = mock(ResultSet.class);
    UserDetailExtractor userDetailExtractor = new UserDetailExtractor(resultSet);
    when(resultSet.next()).thenThrow(new SQLException("some error"));

    userDetailExtractor.extractData();
  }
}
