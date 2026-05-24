package com.antoineromand.atlascrm.authentication.application.usecase.logout;

import com.antoineromand.atlascrm.authentication.application.ITokenRepository;
import com.antoineromand.atlascrm.authentication.application.exceptions.InvalidTokenException;
import com.antoineromand.atlascrm.shared.token.TokenService;
import org.springframework.stereotype.Service;

@Service
public class LogoutUseCase implements ILogoutUseCase {
  private final ITokenRepository tokenRepository;
  private final TokenService tokenService;

  public LogoutUseCase(ITokenRepository tokenRepository, TokenService tokenService) {
    this.tokenRepository = tokenRepository;
    this.tokenService = tokenService;
  }

  @Override
  public void execute(String refreshToken) {
    this.validateToken(refreshToken);
    this.deleteToken(refreshToken);
  }

  private void validateToken(String refreshToken) {
    if (!this.tokenService.isRefreshTokenValid(refreshToken)) {
      throw new InvalidTokenException();
    }
  }

  private void deleteToken(String refreshToken) {
    String jti = this.tokenService.extractJti(refreshToken);
    this.tokenRepository.deleteByJti(jti);
  }
}
