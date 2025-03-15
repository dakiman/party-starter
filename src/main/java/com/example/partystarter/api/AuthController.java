package com.example.partystarter.api;

import com.example.partystarter.exception.DuplicateUserException;
import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.User;
import com.example.partystarter.model.request.LoginRequest;
import com.example.partystarter.model.request.RegisterRequest;
import com.example.partystarter.model.response.AuthResponse;
import com.example.partystarter.repo.UserRepository;
import com.example.partystarter.security.JWTUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerHandler(@RequestBody RegisterRequest request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username is already taken");
        }

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email is already registered");
        }

        String encodedPass = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(encodedPass)
                .build();

        user = userRepo.save(user);
        String token = jwtUtil.generateToken(user.getUsername());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .user(user)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword());

            authManager.authenticate(authInputToken);

            String token = jwtUtil.generateToken(request.getUsername());
            User user = userRepo.getByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "User not found"));

            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .user(user)
                    .build();

            return ResponseEntity.ok(response);
        } catch (AuthenticationException authExc) {
            throw new ResourceException(HttpStatus.UNAUTHORIZED, "Invalid Login Credentials");
        }
    }

    @GetMapping("/user")
    public ResponseEntity<AuthResponse> user() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();

        User user = userRepo
                .getByUsername(username)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "User not found"));
                
        String token = jwtUtil.generateToken(username);
        
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .user(user)
                .build();

        return ResponseEntity.ok(response);
    }

}