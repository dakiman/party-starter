package com.example.partystarter.api;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.partystarter.model.request.LoginRequest;
import com.example.partystarter.model.response.LoginResponse;
import com.example.partystarter.utils.JwtHelper;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
public class AuthController {

    private final JwtHelper jwtHelper;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping(path = "login")
    public LoginResponse login(
            @RequestBody LoginRequest request
            ) {

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        if (passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            Map<String, String> claims = new HashMap<>();
            claims.put("username", request.getUsername());

            String authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            claims.put("authorities", authorities);
            claims.put("userId", String.valueOf(1));

            String token = jwtHelper.createJwtForClaims(request.getUsername(), claims);
            return new LoginResponse(token);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    @GetMapping(path = "user")
    public UserDetails getUser(Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        Map<String, Object> attributes = token.getTokenAttributes();
        return userDetailsService.loadUserByUsername(attributes.get("username").toString());
    }
}