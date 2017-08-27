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

public class EncryptedPasswordExtractorTest {

  // CSOFF: VisibilityModifierCheck
  @Rule
  public ExpectedException resultSetFetchException = ExpectedException.none();
  // CSON: VisibilityModifierCheck

  @Test
  public void getEncryptedPassword_whenResultSetHasData_thenEncryptedPasswordIsReturned() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    EncryptedPasswordExtractor encryptedPasswordExtractor = new EncryptedPasswordExtractor(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getString("encryptedpassword")).thenReturn("some encrypted password");

    String encryptedPassword = encryptedPasswordExtractor.extractData();

    assertThat(encryptedPassword, is(equalTo("some encrypted password")));
  }

  @Test
  public void getEncryptedPassword_whenResultSetHasNoData_thenEncryptedPasswordIsNull() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    EncryptedPasswordExtractor encryptedPasswordExtractor = new EncryptedPasswordExtractor(resultSet);
    when(resultSet.next()).thenReturn(false);

    String encryptedPassword = encryptedPasswordExtractor.extractData();

    assertNull(encryptedPassword);
  }

  @Test
  public void getEncryptedPassword_whenResultSetThrowsException_thenExpectedExceptionIsThrown() throws SQLException {
    resultSetFetchException.expect(SQLException.class);

    ResultSet resultSet = mock(ResultSet.class);
    EncryptedPasswordExtractor encryptedPasswordExtractor = new EncryptedPasswordExtractor(resultSet);
    when(resultSet.next()).thenThrow(new SQLException("some message"));

    encryptedPasswordExtractor.extractData();
  }
}
