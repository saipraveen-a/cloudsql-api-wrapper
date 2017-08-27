package com.google.cloudsql.instance.service;

import com.google.cloudsql.instance.model.CloudSqlDatabase;
import com.google.cloudsql.instance.model.CloudSqlInstance;
import com.google.cloudsql.instance.model.CloudSqlTable;
import com.google.cloudsql.instance.model.CloudSqlTableSchema;
import com.google.cloudsql.instance.model.CloudSqlUser;
import com.google.cloudsql.instance.model.CsvImportContext;

import java.util.List;

public interface CloudSqlAdminService {

  CloudSqlInstance createInstance(CloudSqlInstance cloudSqlInstance);

  CloudSqlInstance getInstance();

  boolean instanceExists(String instanceName);

  void deleteInstanceIfExists(String instanceName);

  CloudSqlUser createUser(String user, String userPassword);

  CloudSqlUser getUser(String user);

  String getEncryptedPassword(String passwordToEncrypt);

  void createDatabase(CloudSqlDatabase cloudSqlDatabase);

  boolean databaseExists(CloudSqlDatabase cloudSqlDatabase);

  void grantAllPrivilegesToUserForDatabase(String user, CloudSqlDatabase cloudSqlDatabase);

  List<String> getPrivilegesForUser(String user);

  void grantUsageAndReloadPrivilegesToUser(String user);

  void createTable(CloudSqlTable cloudSqlTable, CloudSqlTableSchema cloudSqlTableSchema);

  boolean tableExists(CloudSqlTable cloudSqlTable);

  void importCsv(CsvImportContext csvImportContext);
}
