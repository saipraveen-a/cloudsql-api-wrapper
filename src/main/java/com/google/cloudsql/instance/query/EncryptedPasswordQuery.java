package com.google.cloudsql.instance.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EncryptedPasswordQuery extends Query<String> {
  private static final String GET_ENCRYPTED_PASSWORD_QUERY = "select PASSWORD(?) as encryptedpassword";

  private String passwordToEncrypt;

  public EncryptedPasswordQuery(QueryInstanceDetails queryInstanceDetails, String passwordToEncrypt) {
    super(queryInstanceDetails);
    this.passwordToEncrypt = passwordToEncrypt;
  }

  @Override
  protected String executeStatement(PreparedStatement statement) throws SQLException {
    try (ResultSet resultSet = statement.executeQuery();) {
      return getExtractor(resultSet).extractData();
    }
  }

  protected Extractor<String> getExtractor(ResultSet resultSet) {
    return new EncryptedPasswordExtractor(resultSet);
  }

  @Override
  protected void bindParameters(PreparedStatement statement) throws SQLException {
    statement.setString(1, passwordToEncrypt);
  }

  @Override
  protected String getQuery() {
    return GET_ENCRYPTED_PASSWORD_QUERY;
  }
}
