package com.antoineromand.atlascrm.authentication.infrastructure.web;

import com.antoineromand.atlascrm.shared.token.TokenPayload;
import com.antoineromand.atlascrm.shared.token.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenService tokenService;

  public JwtAuthenticationFilter(TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = this.extractBearerToken(request.getHeader("Authorization"));

    if (token != null && this.tokenService.isAccessTokenValid(token)) {
      TokenPayload payload = this.tokenService.extractPayload(token);
      var authorities =
          List.of(new SimpleGrantedAuthority("ROLE_" + payload.getRole().toUpperCase()));
      var authentication =
          new UsernamePasswordAuthenticationToken(payload.getUserId().toString(), null, authorities);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private String extractBearerToken(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return null;
    }
    return authorizationHeader.substring(7);
  }
}
