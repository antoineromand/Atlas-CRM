package com.antoineromand.atlascrm.authentication.domain.valueobject;

public enum RoleName {
  ADMIN,
  USER;

  public boolean isAdmin() {
    return this == ADMIN;
  }
}
