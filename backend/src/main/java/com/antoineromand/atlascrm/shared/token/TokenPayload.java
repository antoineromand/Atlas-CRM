package com.antoineromand.atlascrm.shared.token;

import java.util.UUID;

public class TokenPayload {
  private final UUID userId;
  private final String role;

  public TokenPayload(UUID userId, String role) {
    this.userId = userId;
    this.role = role;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getRole() {
    return role;
  }
}
