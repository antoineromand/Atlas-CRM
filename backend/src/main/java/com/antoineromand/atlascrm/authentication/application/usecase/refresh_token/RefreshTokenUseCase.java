package com.antoineromand.atlascrm.authentication.application.usecase.refresh_token;

import com.antoineromand.atlascrm.authentication.application.ITokenRepository;
import com.antoineromand.atlascrm.authentication.application.TokenPair;
import com.antoineromand.atlascrm.authentication.application.exceptions.CredentialsNotFoundException;
import com.antoineromand.atlascrm.authentication.application.exceptions.InvalidTokenException;
import com.antoineromand.atlascrm.authentication.application.exceptions.JtiNotFoundInCacheException;
import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.authentication.domain.repository.ICredentialsRepository;
import com.antoineromand.atlascrm.shared.token.TokenService;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefreshTokenUseCase implements IRefreshUseCase {

  private final ITokenRepository tokenRepository;
  private final ICredentialsRepository credentialsRepository;
  private final TokenService tokenService;

  public RefreshTokenUseCase(
      ITokenRepository tokenRepository,
      ICredentialsRepository credentialsRepository,
      TokenService tokenService) {
    this.tokenRepository = tokenRepository;
    this.credentialsRepository = credentialsRepository;
    this.tokenService = tokenService;
  }

  @Override
  public TokenPair execute(String token) {
    this.validateToken(token);
    String jti = this.extractJti(token);
    String userId = this.getUserIdFromCache(jti);
    Credentials credentials = this.getCredentials(userId);

    Duration remainingExpiration = this.getRemainingDuration(token);
    this.deleteOldJti(jti);

    TokenPair newTokens = this.createRotatedTokens(credentials, remainingExpiration);
    this.storeNewJti(newTokens.refreshToken(), credentials.getId(), remainingExpiration);

    return newTokens;
  }

  private void validateToken(String token) {
    if (!this.tokenService.isRefreshTokenValid(token)) {
      throw new InvalidTokenException();
    }
  }

  private String extractJti(String token) {
    return this.tokenService.extractJti(token);
  }

  private String getUserIdFromCache(String jti) {
    String userId = this.tokenRepository.getUserIdByJti(jti);
    if (userId == null) {
      throw new JtiNotFoundInCacheException();
    }
    return userId;
  }

  private Credentials getCredentials(String userId) {
    return this.credentialsRepository.findByUserId(userId).orElseThrow(CredentialsNotFoundException::new);
  }

  private void deleteOldJti(String jti) {
    this.tokenRepository.deleteByJti(jti);
  }

  private void storeNewJti(String refreshToken, UUID userId, Duration ttl) {
    String newJti = this.tokenService.extractJti(refreshToken);
    this.tokenRepository.storeRefreshToken(newJti, userId.toString(), ttl);
  }

  private TokenPair createRotatedTokens(Credentials credentials, Duration ttl) {
    String access = this.tokenService.createAccessToken(credentials.getId(), credentials.getRole().name());
    String refresh = this.tokenService.createRefreshToken(credentials.getId(), credentials.getRole().name(), ttl);
    return new TokenPair(access, refresh);
  }

  private Duration getRemainingDuration(String oldRefreshToken) {
    Date oldExpiration = this.tokenService.extractExpiration(oldRefreshToken);
    Instant remainingExpiration = oldExpiration.toInstant();
    if (remainingExpiration.isBefore(Instant.now())) {
      throw new InvalidTokenException();
    }
    return Duration.between(Instant.now(), remainingExpiration);
  }
}
