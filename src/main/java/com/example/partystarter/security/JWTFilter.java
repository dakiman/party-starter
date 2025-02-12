package com.example.partystarter.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    private final CustomUserDetailsService userDetailsService;
    private final JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // List of public paths that should not be filtered
        String[] publicPaths = {
                "/auth/login",
                "/auth/register",
                "/drinks",
                "/ingredients",
                "/swagger-ui/**",
                "/v3/api-docs/**"
        };

        String path = request.getServletPath();

        for (String pattern : publicPaths) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            logger.debug("Processing request to: {} {}", request.getMethod(), request.getRequestURI());
            logger.debug("Authorization header: {}", authHeader != null ? "present" : "missing");

            // If no auth header or not a protected endpoint, continue with chain
            if (authHeader == null || shouldNotFilter(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Validate Bearer token format
            if (!authHeader.startsWith(BEARER_PREFIX)) {
                throw new JWTVerificationException("Invalid authorization header format");
            }

            String jwt = authHeader.substring(BEARER_PREFIX.length());
            String username = jwtUtil.validateTokenAndRetrieveSubject(jwt);
            logger.debug("JWT validation successful for username: {}", username);

            // Set authentication if not already set
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (JWTVerificationException e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error in JWT filter: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
}