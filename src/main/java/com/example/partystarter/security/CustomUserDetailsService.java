package com.example.partystarter.security;

import com.example.partystarter.model.User;
import com.example.partystarter.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find user by username first, then by email if not found
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