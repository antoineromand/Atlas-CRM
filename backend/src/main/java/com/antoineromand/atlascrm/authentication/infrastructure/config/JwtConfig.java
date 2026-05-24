package com.antoineromand.atlascrm.authentication.infrastructure.config;

import com.antoineromand.atlascrm.shared.token.TokenProvider;
import com.antoineromand.atlascrm.shared.token.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
  @Bean
  public TokenProvider tokenProvider(JwtProperties properties) {
    return new TokenProvider(properties.getSecret(), properties.getAccessExpiration());
  }

  @Bean
  public TokenService tokenService(TokenProvider tokenProvider) {
    return new TokenService(tokenProvider);
  }
}
