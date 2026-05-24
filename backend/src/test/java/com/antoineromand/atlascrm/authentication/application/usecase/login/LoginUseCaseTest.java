package com.antoineromand.atlascrm.authentication.application.usecase.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antoineromand.atlascrm.authentication.application.ITokenRepository;
import com.antoineromand.atlascrm.authentication.application.TokenPair;
import com.antoineromand.atlascrm.authentication.application.exceptions.CredentialsNotActiveException;
import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.authentication.domain.repository.ICredentialsRepository;
import com.antoineromand.atlascrm.authentication.domain.service.IPasswordService;
import com.antoineromand.atlascrm.authentication.domain.valueobject.CredentialsStatus;
import com.antoineromand.atlascrm.authentication.domain.valueobject.RoleName;
import com.antoineromand.atlascrm.authentication.infrastructure.config.JwtProperties;
import com.antoineromand.atlascrm.shared.token.TokenService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

  @Mock private IPasswordService passwordService;
  @Mock private ICredentialsRepository credentialsRepository;
  @Mock private TokenService tokenService;
  @Mock private ITokenRepository tokenRepository;

  private final JwtProperties jwtProperties = new JwtProperties();

  @Test
  void executeShouldReturnTokenPairAndStoreRefreshToken() {
    jwtProperties.setRefreshExpiration(Duration.ofDays(7));
    LoginUseCase loginUseCase =
        new LoginUseCase(
            passwordService, credentialsRepository, tokenService, tokenRepository, jwtProperties);

    LoginCommand command = new LoginCommand("user@example.com", "Password123!");
    UUID credentialsId = UUID.randomUUID();
    Credentials credentials =
        new Credentials(
            credentialsId,
            command.email(),
            "hashed-password",
            RoleName.USER,
            Instant.now(),
            null,
            CredentialsStatus.ACTIVE,
            true);

    when(credentialsRepository.findByEmail(command.email())).thenReturn(Optional.of(credentials));
    when(passwordService.matches(command.password(), credentials.getPassword())).thenReturn(true);
    when(tokenService.createAccessToken(credentialsId, credentials.getRole().name()))
        .thenReturn("access-token");
    when(tokenService.createRefreshToken(credentialsId, credentials.getRole().name(), Duration.ofDays(7)))
        .thenReturn("refresh-token");
    when(tokenService.extractJti("refresh-token")).thenReturn("refresh-jti");

    TokenPair result = loginUseCase.execute(command);

    assertEquals("access-token", result.accessToken());
    assertEquals("refresh-token", result.refreshToken());
    verify(tokenRepository)
        .storeRefreshToken("refresh-jti", credentialsId.toString(), Duration.ofDays(7));
  }

  @Test
  void executeShouldRejectInactiveCredentials() {
    jwtProperties.setRefreshExpiration(Duration.ofDays(7));
    LoginUseCase loginUseCase =
        new LoginUseCase(
            passwordService, credentialsRepository, tokenService, tokenRepository, jwtProperties);

    LoginCommand command = new LoginCommand("user@example.com", "Password123!");
    Credentials credentials =
        new Credentials(
            UUID.randomUUID(),
            command.email(),
            "hashed-password",
            RoleName.USER,
            Instant.now(),
            null,
            CredentialsStatus.SUSPENDED,
            true);

    when(credentialsRepository.findByEmail(command.email())).thenReturn(Optional.of(credentials));

    assertThrows(CredentialsNotActiveException.class, () -> loginUseCase.execute(command));
    verify(passwordService, never()).matches(any(), any());
    verify(tokenRepository, never()).storeRefreshToken(anyString(), anyString(), any(Duration.class));
  }
}
