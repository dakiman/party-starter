package com.example.partystarter.security;

import com.example.partystarter.model.User;
import com.example.partystarter.repo.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Optional<User> userRes = userRepo.getByUsername(usernameOrEmail);
        if(userRes.isEmpty()) {
            userRes = userRepo.findByEmail(usernameOrEmail);
        }
        
        if(userRes.isEmpty()) {
            throw new UsernameNotFoundException("Could not find user with username/email: " + usernameOrEmail);
        }
        
        User user = userRes.get();
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),  // Use username consistently
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}