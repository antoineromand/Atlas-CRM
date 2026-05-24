package com.antoineromand.atlascrm.authentication.application.usecase.login;

import com.antoineromand.atlascrm.authentication.application.ITokenRepository;
import com.antoineromand.atlascrm.authentication.application.TokenPair;
import com.antoineromand.atlascrm.authentication.application.exceptions.CredentialsNotActiveException;
import com.antoineromand.atlascrm.authentication.application.exceptions.CredentialsNotFoundException;
import com.antoineromand.atlascrm.authentication.application.exceptions.EmailNotVerifiedException;
import com.antoineromand.atlascrm.authentication.application.exceptions.PasswordNotMatchException;
import com.antoineromand.atlascrm.authentication.domain.Credentials;
import com.antoineromand.atlascrm.authentication.domain.repository.ICredentialsRepository;
import com.antoineromand.atlascrm.authentication.domain.service.IPasswordService;
import com.antoineromand.atlascrm.authentication.infrastructure.config.JwtProperties;
import com.antoineromand.atlascrm.shared.token.TokenService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LoginUseCase implements ILoginUseCase {
  private final IPasswordService passwordService;
  private final ICredentialsRepository credentialsRepository;
  private final TokenService tokenService;
  private final ITokenRepository tokenRepository;
  private final JwtProperties jwtProperties;

  public LoginUseCase(
      IPasswordService passwordService,
      ICredentialsRepository credentialsRepository,
      TokenService tokenService,
      ITokenRepository tokenRepository,
      JwtProperties jwtProperties) {
    this.passwordService = passwordService;
    this.credentialsRepository = credentialsRepository;
    this.tokenService = tokenService;
    this.tokenRepository = tokenRepository;
    this.jwtProperties = jwtProperties;
  }

  @Override
  public TokenPair execute(LoginCommand command) {
    Credentials credentials = this.getCredentials(command.email());
    this.checkActive(credentials);
    this.checkEmailVerified(credentials);
    this.checkPassword(command.password(), credentials.getPassword());
    return this.generateTokens(credentials.getId(), credentials.getRole().name());
  }

  private Credentials getCredentials(String email) {
    return this.credentialsRepository.findByEmail(email).orElseThrow(CredentialsNotFoundException::new);
  }

  private void checkPassword(String rawPassword, String hashedPassword) {
    if (!this.passwordService.matches(rawPassword, hashedPassword)) {
      throw new PasswordNotMatchException();
    }
  }

  private void checkActive(Credentials credentials) {
    if (!credentials.isActive()) {
      throw new CredentialsNotActiveException();
    }
  }

  private void checkEmailVerified(Credentials credentials) {
    if (!credentials.isEmailVerified()) {
      throw new EmailNotVerifiedException();
    }
  }

  private TokenPair generateTokens(UUID id, String role) {
    String accessToken = this.tokenService.createAccessToken(id, role);
    String refreshToken =
        this.tokenService.createRefreshToken(id, role, this.jwtProperties.getRefreshExpiration());
    this.storeRefreshTokenInCache(this.tokenService.extractJti(refreshToken), id.toString());
    return new TokenPair(accessToken, refreshToken);
  }

  private void storeRefreshTokenInCache(String jti, String userId) {
    this.tokenRepository.storeRefreshToken(
        jti, userId, this.jwtProperties.getRefreshExpiration());
  }
}
