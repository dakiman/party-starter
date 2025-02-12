package com.example.partystarter.api;

import com.example.partystarter.exception.DuplicateUserException;
import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.User;
import com.example.partystarter.model.request.LoginRequest;
import com.example.partystarter.model.request.RegisterRequest;
import com.example.partystarter.repo.UserRepository;
import com.example.partystarter.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Map<String, Object> registerHandler(@RequestBody RegisterRequest request) {
        // Check if username exists
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username is already taken");
        }

        // Check if email exists
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email is already registered");
        }

        // Create new user
        String encodedPass = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(encodedPass)
//                .roles("ROLE_USER")
                .build();

        user = userRepo.save(user);
        String token = jwtUtil.generateToken(user.getUsername());

        return Map.of(
            "token", token,
            "user", user
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        try {
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword());

            authManager.authenticate(authInputToken);

            String token = jwtUtil.generateToken(request.getUsername());
            User user = userRepo.getByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "User not found"));

            return Map.of(
                "token", token,
                "user", user
            );
        } catch (AuthenticationException authExc) {
            throw new ResourceException(HttpStatus.UNAUTHORIZED, "Invalid Login Credentials");
        }
    }

    @GetMapping("/user")
    public User user() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();

        return userRepo
                .getByUsername(username)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "User not found"));
    }

}