package com.antoineromand.atlascrm.shared.token;

import java.time.Duration;
import java.util.Date;

public interface ITokenProvider {
  String generateAccessToken(TokenPayload payload);

  String generateRefreshToken(TokenPayload payload, Duration dynamicExpiration);

  boolean verifyAccessToken(String accessToken);

  boolean verifyRefreshToken(String refreshToken);

  TokenPayload decodeAccessToken(String accessToken);

  String extractJtiFromToken(String refreshToken);

  Date extractExpirationFromToken(String jwtToken);
}
