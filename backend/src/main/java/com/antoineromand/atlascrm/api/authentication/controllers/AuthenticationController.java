package com.antoineromand.atlascrm.api.authentication.controllers;

import com.antoineromand.atlascrm.api.authentication.dto.LoginRequestDto;
import com.antoineromand.atlascrm.api.authentication.dto.RegisterRequestDto;
import com.antoineromand.atlascrm.api.authentication.dto.RegisterResponseDto;
import com.antoineromand.atlascrm.api.shared.MessageResponse;
import com.antoineromand.atlascrm.authentication.application.TokenPair;
import com.antoineromand.atlascrm.authentication.application.usecase.login.ILoginUseCase;
import com.antoineromand.atlascrm.authentication.application.usecase.login.LoginCommand;
import com.antoineromand.atlascrm.authentication.application.usecase.logout.ILogoutUseCase;
import com.antoineromand.atlascrm.authentication.application.usecase.refresh_token.IRefreshUseCase;
import com.antoineromand.atlascrm.authentication.application.usecase.register.IRegisterUseCase;
import com.antoineromand.atlascrm.authentication.application.usecase.register.RegisterCommand;
import com.antoineromand.atlascrm.authentication.infrastructure.config.JwtProperties;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authentication")
public class AuthenticationController {
  private static final String REFRESH_COOKIE_NAME = "atlas_refresh_token";
  private static final String REFRESH_COOKIE_PATH = "/api/v1/authentication";

  private final IRegisterUseCase registerUseCase;
  private final ILoginUseCase loginUseCase;
  private final IRefreshUseCase refreshUseCase;
  private final ILogoutUseCase logoutUseCase;
  private final JwtProperties jwtProperties;

  @Value("${atlascrm.security.refresh-cookie.secure:false}")
  private boolean refreshCookieSecure;

  public AuthenticationController(
      IRegisterUseCase registerUseCase,
      ILoginUseCase loginUseCase,
      IRefreshUseCase refreshUseCase,
      ILogoutUseCase logoutUseCase,
      JwtProperties jwtProperties) {
    this.registerUseCase = registerUseCase;
    this.loginUseCase = loginUseCase;
    this.refreshUseCase = refreshUseCase;
    this.logoutUseCase = logoutUseCase;
    this.jwtProperties = jwtProperties;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
    UUID credentialsId =
        this.registerUseCase.execute(
            new RegisterCommand(dto.email(), dto.password(), dto.firstName(), dto.lastName()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new RegisterResponseDto(
                credentialsId, "Registration completed successfully. Please verify your email."));
  }

  @PostMapping("/sign-in")
  public ResponseEntity<TokenPair> signIn(@Valid @RequestBody LoginRequestDto dto) {
    TokenPair tokenPair = this.loginUseCase.execute(new LoginCommand(dto.email(), dto.password()));
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, this.buildRefreshTokenCookie(tokenPair.refreshToken()).toString())
        .body(tokenPair);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<TokenPair> refreshToken(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String refreshTokenHeader,
      @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshTokenCookie) {
    String refreshToken = this.resolveRefreshToken(refreshTokenHeader, refreshTokenCookie);
    TokenPair tokenPair = this.refreshUseCase.execute(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, this.buildRefreshTokenCookie(tokenPair.refreshToken()).toString())
        .body(tokenPair);
  }

  @PostMapping("/sign-out")
  public ResponseEntity<MessageResponse> signOut(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String refreshTokenHeader,
      @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshTokenCookie) {
    String refreshToken = this.resolveRefreshToken(refreshTokenHeader, refreshTokenCookie);
    this.logoutUseCase.execute(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, this.clearRefreshTokenCookie().toString())
        .body(new MessageResponse("Sign out successful."));
  }

  private String resolveRefreshToken(String authorizationHeader, String refreshTokenCookie) {
    if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
      return refreshTokenCookie;
    }

    if (authorizationHeader != null && !authorizationHeader.isBlank()) {
      return authorizationHeader.startsWith("Bearer ")
          ? authorizationHeader.substring(7)
          : authorizationHeader;
    }

    throw new IllegalArgumentException("Refresh token is required.");
  }

  private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(this.refreshCookieSecure)
        .sameSite("Lax")
        .path(REFRESH_COOKIE_PATH)
        .maxAge(this.jwtProperties.getRefreshExpiration())
        .build();
  }

  private ResponseCookie clearRefreshTokenCookie() {
    return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(this.refreshCookieSecure)
        .sameSite("Lax")
        .path(REFRESH_COOKIE_PATH)
        .maxAge(0)
        .build();
  }
}
