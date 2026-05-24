package com.antoineromand.atlascrm.shared.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

public class TokenProvider implements ITokenProvider {

  private final String secret;
  private final Duration accessExpiration;

  public TokenProvider(String secret, Duration accessExpiration) {
    this.secret = secret;
    this.accessExpiration = accessExpiration;
  }

  @Override
  public String generateAccessToken(TokenPayload payload) {
    return buildToken(payload, accessExpiration.toMillis());
  }

  @Override
  public String generateRefreshToken(TokenPayload payload, Duration dynamicExpiration) {
    return buildToken(payload, dynamicExpiration.toMillis());
  }

  @Override
  public boolean verifyAccessToken(String accessToken) {
    return validate(accessToken);
  }

  @Override
  public boolean verifyRefreshToken(String refreshToken) {
    return validate(refreshToken);
  }

  @Override
  public TokenPayload decodeAccessToken(String accessToken) {
    Claims claims =
        Jwts.parser().verifyWith(this.getSigningKey()).build().parseSignedClaims(accessToken)
            .getPayload();
    return new TokenPayload(UUID.fromString(claims.getSubject()), claims.get("role").toString());
  }

  @Override
  public String extractJtiFromToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(this.getSigningKey()).build().parseSignedClaims(token)
            .getPayload();
    return claims.getId();
  }

  @Override
  public Date extractExpirationFromToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(this.getSigningKey()).build().parseSignedClaims(token)
            .getPayload();
    return claims.getExpiration();
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(this.secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private String buildToken(TokenPayload payload, long expirationMs) {
    Instant now = Instant.now();
    String jti = UUID.randomUUID().toString();
    return Jwts.builder()
        .id(jti)
        .subject(payload.getUserId().toString())
        .claim("role", payload.getRole())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(expirationMs)))
        .signWith(this.getSigningKey())
        .compact();
  }

  private boolean validate(String token) {
    if (token == null || token.isBlank()) {
      return false;
    }

    try {
      Claims claims =
          Jwts.parser().verifyWith(this.getSigningKey()).build().parseSignedClaims(token)
              .getPayload();
      return !claims.getExpiration().before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
