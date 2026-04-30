package com.example.partystarter.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JWTFilter filter;
    private final CustomUserDetailsService uds;

    @Value("${application.security.allowed-origins}")
    private String allowedOriginsCsv;

    public SecurityConfig(CustomUserDetailsService uds, JWTFilter filter) {
        this.uds = uds;
        this.filter = filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // ── Public endpoints ─────────────────────────────────
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/register").permitAll()

                        // /drinks and /ingredients are reference-catalogue endpoints with no PII.
                        // Public read is intentional. Rate limiting is deferred to Phase 9.
                        .requestMatchers("/drinks/**").permitAll()
                        .requestMatchers("/ingredients/**").permitAll()

                        // /music/** is a passthrough to Spotify search/genres. Public for now;
                        // becomes auth-only in Phase 8 once playlists tie to user accounts.
                        .requestMatchers("/music/**").permitAll()

                        // OpenAPI docs
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()

                        // Container healthcheck (added in Phase 0; do NOT remove or the
                        // docker-compose healthcheck on /actuator/health 401s and the
                        // container is reported unhealthy forever).
                        .requestMatchers("/actuator/health").permitAll()

                        // ── Authenticated endpoints ──────────────────────────
                        .requestMatchers("/auth/user").authenticated()
                        .requestMatchers("/events/**").authenticated()

                        // Default: deny anything not matched above
                        .anyRequest().authenticated())
                .userDetailsService(uds)
                .exceptionHandling(exc -> exc
                    .authenticationEntryPoint((request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                )
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = List.of(allowedOriginsCsv.split("\\s*,\\s*"));

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
