package com.google.cloudsql.instance.model;

public final class CsvImportContext {
  private CloudSqlTable cloudSqlTable;
  private String gcsUri;

  private CsvImportContext(CloudSqlTable cloudSqlTable, String gcsUri) {
    this.cloudSqlTable = cloudSqlTable;
    this.gcsUri = gcsUri;
  }

  public CloudSqlTable getCloudSqlTable() {
    return cloudSqlTable;
  }

  public String getGcsUri() {
    return gcsUri;
  }

  public static class Builder {
    private CloudSqlTable cloudSqlTable;
    private String gcsUri;

    public Builder() {}

    public Builder setCloudSqlTable(CloudSqlTable cloudSqlTable) {
      this.cloudSqlTable = cloudSqlTable;
      return this;
    }

    public Builder setGcsUri(String gcsUri) {
      this.gcsUri = gcsUri;
      return this;
    }

    public CsvImportContext build() {
      return new CsvImportContext(cloudSqlTable, gcsUri);
    }
  }
}
