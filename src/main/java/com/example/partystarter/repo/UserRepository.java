package com.example.partystarter.repo;

import com.example.partystarter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> getByUsername(String username);
}
