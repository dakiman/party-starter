package com.example.partystarter.repo;

import com.example.partystarter.model.GuestUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestUserRepository extends JpaRepository<GuestUser, Long> {
    Optional<GuestUser> findByGuestToken(String guestToken);
}
