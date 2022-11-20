package com.example.partystarter.api;

import com.example.partystarter.model.User;
import com.example.partystarter.model.request.LoginRequest;
import com.example.partystarter.model.request.RegisterRequest;
import com.example.partystarter.repo.UserRepository;
import com.example.partystarter.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    public Map<String, Object> register(@RequestBody RegisterRequest request){
        String encodedPass = passwordEncoder.encode(request.getPassword());
        request.setPassword(encodedPass);
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .build();
        user = userRepo.save(user);
        String token = jwtUtil.generateToken(user.getEmail());
        return Collections.singletonMap("token", token);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request){
        try {
            UsernamePasswordAuthenticationToken authInputToken =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

            authManager.authenticate(authInputToken);

            String token = jwtUtil.generateToken(request.getUsername());

            return Collections.singletonMap("token", token);
        }catch (AuthenticationException authExc){
            throw new RuntimeException("Invalid Login Credentials");
        }
    }

    @GetMapping("/user")
    public User user(){
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepo.getByUsername(username).get();
    }


}