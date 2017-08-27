package com.google.cloudsql.instance.query;

import com.google.cloudsql.instance.model.CloudSqlDatabase;
import com.google.cloudsql.instance.model.CloudSqlTable;
import com.google.cloudsql.instance.model.CloudSqlTableSchema;
import com.google.cloudsql.instance.model.CloudSqlUser;

import java.util.List;

public class QueryFactory {

  public Query<CloudSqlUser> getUserDetailQuery(QueryInstanceDetails queryInstanceDetails, String user) {
    return new UserDetailQuery(queryInstanceDetails, user);
  }

  public Query<String> getEncryptedPasswordQuery(
      QueryInstanceDetails queryInstanceDetails, String passwordToEncrypt) {
    return new EncryptedPasswordQuery(queryInstanceDetails, passwordToEncrypt);
  }

  public Query getCreateDatabaseQuery(QueryInstanceDetails queryInstanceDetails, String databaseName) {
    return new CreateDatabaseQuery(queryInstanceDetails, databaseName);
  }

  public Query getDatabaseQuery(QueryInstanceDetails queryInstanceDetails, String name) {
    return new GetDatabaseQuery(queryInstanceDetails, name);
  }

  public Query getGrantAllPrivilegesToUserOnDatabaseQuery(
      QueryInstanceDetails queryInstanceDetails, String user, String databaseName) {
    return new GrantAllPrivilegesQuery(queryInstanceDetails, user, databaseName);
  }

  public Query<List<String>> getUserPrivilegesQuery(QueryInstanceDetails queryInstanceDetails, String user) {
    return new GetUserPrivilegesQuery(queryInstanceDetails, user);
  }

  public Query getGrantReloadPrivilegesToUserOnDatabaseQuery(
      QueryInstanceDetails queryInstanceDetails, String user) {
    return new GrantUsageAndReloadPrivilegesQuery(
        queryInstanceDetails, user);
  }

  public Query getCreateTableQuery(
      QueryInstanceDetails queryInstanceDetails,
      CloudSqlDatabase cloudSqlDatabase,
      CloudSqlTable cloudSqlTable,
      CloudSqlTableSchema cloudSqlTableSchema) {
    return new CreateTableQuery(
        queryInstanceDetails, cloudSqlDatabase, cloudSqlTable, cloudSqlTableSchema);
  }

  public Query getFetchTableQuery(QueryInstanceDetails queryInstanceDetails, CloudSqlDatabase database,
                                  CloudSqlTable table) {
    return new GetTableQuery(queryInstanceDetails, database, table);
  }
}
