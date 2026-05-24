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
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authentication")
public class AuthenticationController {
  private final IRegisterUseCase registerUseCase;
  private final ILoginUseCase loginUseCase;
  private final IRefreshUseCase refreshUseCase;
  private final ILogoutUseCase logoutUseCase;

  public AuthenticationController(
      IRegisterUseCase registerUseCase,
      ILoginUseCase loginUseCase,
      IRefreshUseCase refreshUseCase,
      ILogoutUseCase logoutUseCase) {
    this.registerUseCase = registerUseCase;
    this.loginUseCase = loginUseCase;
    this.refreshUseCase = refreshUseCase;
    this.logoutUseCase = logoutUseCase;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
    UUID credentialsId =
        this.registerUseCase.execute(new RegisterCommand(dto.email(), dto.password()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new RegisterResponseDto(
                credentialsId, "Registration completed successfully. Please verify your email."));
  }

  @PostMapping("/sign-in")
  public ResponseEntity<TokenPair> signIn(@Valid @RequestBody LoginRequestDto dto) {
    TokenPair tokenPair = this.loginUseCase.execute(new LoginCommand(dto.email(), dto.password()));
    return ResponseEntity.ok(tokenPair);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<TokenPair> refreshToken(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String refreshTokenHeader) {
    String refreshToken = this.extractBearerToken(refreshTokenHeader);
    TokenPair tokenPair = this.refreshUseCase.execute(refreshToken);
    return ResponseEntity.ok(tokenPair);
  }

  @PostMapping("/sign-out")
  public ResponseEntity<MessageResponse> signOut(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String refreshTokenHeader) {
    String refreshToken = this.extractBearerToken(refreshTokenHeader);
    this.logoutUseCase.execute(refreshToken);
    return ResponseEntity.ok(new MessageResponse("Sign out successful."));
  }

  private String extractBearerToken(String authorizationHeader) {
    return authorizationHeader.startsWith("Bearer ")
        ? authorizationHeader.substring(7)
        : authorizationHeader;
  }
}
