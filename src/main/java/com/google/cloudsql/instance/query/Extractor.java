package com.google.cloudsql.instance.query;

import java.sql.SQLException;

public interface Extractor<T> {
  T extractData() throws SQLException;
}
