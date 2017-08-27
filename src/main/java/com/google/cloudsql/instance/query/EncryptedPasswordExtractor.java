package com.google.cloudsql.instance.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EncryptedPasswordExtractor implements Extractor<String> {
  private ResultSet resultSet;

  public EncryptedPasswordExtractor(ResultSet resultSet) {
    this.resultSet = resultSet;
  }

  @Override
  public String extractData() throws SQLException {
    if (resultSet.next()) {
      return resultSet.getString("encryptedpassword");
    }
    return null;
  }
}
