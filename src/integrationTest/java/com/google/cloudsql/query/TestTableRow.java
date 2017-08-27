package com.google.cloudsql.query;


public class TestTableRow {

  private int id;
  private String name;

  public TestTableRow() {}

  public TestTableRow(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "TestTableRow{" + "id=" + id + ", name='" + name + '\'' + '}';
  }
}
