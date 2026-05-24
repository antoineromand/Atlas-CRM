package com.antoineromand.atlascrm.authentication.application.usecase.refresh_token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.authentication.application.ITokenRepository;
import com.antoineromand.atlascrm.authentication.application.TokenPair;
import com.antoineromand.atlascrm.authentication.application.exceptions.JtiNotFoundInCacheException;
import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.authentication.domain.repository.ICredentialsRepository;
import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import com.antoineromand.atlascrm.shared.token.TokenService;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

  @Mock private ITokenRepository tokenRepository;
  @Mock private ICredentialsRepository credentialsRepository;
  @Mock private TokenService tokenService;

  @Test
  void executeShouldRotateRefreshTokenAndStoreNewJti() {
    RefreshTokenUseCase refreshTokenUseCase =
        new RefreshTokenUseCase(tokenRepository, credentialsRepository, tokenService);

    String oldRefreshToken = "old-refresh-token";
    UUID userId = UUID.randomUUID();
    Credentials credentials =
        new Credentials(
            userId,
            "user@example.com",
            "hashed-password",
            RoleName.USER,
            Instant.now(),
            null,
            CredentialsStatus.ACTIVE,
            true);

    when(tokenService.isRefreshTokenValid(oldRefreshToken)).thenReturn(true);
    when(tokenService.extractJti(oldRefreshToken)).thenReturn("old-jti");
    when(tokenRepository.getUserIdByJti("old-jti")).thenReturn(userId.toString());
    when(credentialsRepository.findByUserId(userId.toString())).thenReturn(Optional.of(credentials));
    when(tokenService.extractExpiration(oldRefreshToken))
        .thenReturn(Date.from(Instant.now().plus(Duration.ofDays(1))));
    when(tokenService.createAccessToken(userId, credentials.getRole().name()))
        .thenReturn("new-access-token");
    when(tokenService.createRefreshToken(eq(userId), eq(credentials.getRole().name()), any(Duration.class)))
        .thenReturn("new-refresh-token");
    when(tokenService.extractJti("new-refresh-token")).thenReturn("new-jti");

    TokenPair result = refreshTokenUseCase.execute(oldRefreshToken);

    assertEquals("new-access-token", result.accessToken());
    assertEquals("new-refresh-token", result.refreshToken());
    verify(tokenRepository).deleteByJti("old-jti");
    verify(tokenRepository)
        .storeRefreshToken(anyString(), eq(userId.toString()), any(Duration.class));
  }

  @Test
  void executeShouldFailWhenRefreshTokenJtiIsMissingFromCache() {
    RefreshTokenUseCase refreshTokenUseCase =
        new RefreshTokenUseCase(tokenRepository, credentialsRepository, tokenService);

    String oldRefreshToken = "old-refresh-token";
    when(tokenService.isRefreshTokenValid(oldRefreshToken)).thenReturn(true);
    when(tokenService.extractJti(oldRefreshToken)).thenReturn("old-jti");
    when(tokenRepository.getUserIdByJti("old-jti")).thenReturn(null);

    assertThrows(JtiNotFoundInCacheException.class, () -> refreshTokenUseCase.execute(oldRefreshToken));
    verify(credentialsRepository, never()).findByUserId(any());
  }
}
