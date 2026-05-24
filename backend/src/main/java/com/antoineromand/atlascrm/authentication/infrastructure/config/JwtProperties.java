package com.antoineromand.atlascrm.authentication.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atlascrm.security.jwt")
public class JwtProperties {
  private String secret;
  private Duration accessExpiration;
  private Duration refreshExpiration;

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public Duration getAccessExpiration() {
    return accessExpiration;
  }

  public void setAccessExpiration(Duration accessExpiration) {
    this.accessExpiration = accessExpiration;
  }

  public Duration getRefreshExpiration() {
    return refreshExpiration;
  }

  public void setRefreshExpiration(Duration refreshExpiration) {
    this.refreshExpiration = refreshExpiration;
  }
}
