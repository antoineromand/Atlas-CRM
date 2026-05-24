package com.antoineromand.atlascrm.shared.token;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

public class TokenService {
  private final ITokenProvider tokenProvider;

  public TokenService(ITokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  public String createAccessToken(UUID id, String role) {
    return this.tokenProvider.generateAccessToken(new TokenPayload(id, role));
  }

  public String createRefreshToken(UUID id, String role, Duration dynamicExpiration) {
    return this.tokenProvider.generateRefreshToken(new TokenPayload(id, role), dynamicExpiration);
  }

  public boolean isAccessTokenValid(String accessToken) {
    return this.tokenProvider.verifyAccessToken(accessToken);
  }

  public boolean isRefreshTokenValid(String refreshToken) {
    return this.tokenProvider.verifyRefreshToken(refreshToken);
  }

  public TokenPayload extractPayload(String accessToken) {
    return this.tokenProvider.decodeAccessToken(accessToken);
  }

  public String extractJti(String refreshToken) {
    return this.tokenProvider.extractJtiFromToken(refreshToken);
  }

  public Date extractExpiration(String accessToken) {
    return this.tokenProvider.extractExpirationFromToken(accessToken);
  }
}
