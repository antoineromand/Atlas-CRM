package com.antoineromand.atlascrm.shared.config;

import com.antoineromand.atlascrm.authentication.infrastructure.web.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Value("${atlascrm.cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(FormLoginConfigurer::disable);
        http.sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(
            exceptions ->
                exceptions.authenticationEntryPoint(
                    (request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) ->
                            response.sendError(HttpServletResponse.SC_FORBIDDEN)));
        http.authorizeHttpRequests(
            auth -> auth.requestMatchers("/api/v1/authentication/**").permitAll()
                .anyRequest().authenticated());
        http.addFilterBefore(
            jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(
            List.of(allowedOriginPatterns.split("\\s*,\\s*")));
        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
