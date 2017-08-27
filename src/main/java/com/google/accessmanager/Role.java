package com.google.accessmanager;

public enum Role {
  EDITOR("roles/editor");

  private final String roleName;

  Role(String roleName) {
    this.roleName = roleName;
  }

  public String getRoleName() {
    return roleName;
  }
}
