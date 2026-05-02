package com.example.partystarter.repo;

import com.example.partystarter.model.Attendee;
import com.example.partystarter.model.GuestUser;
import com.example.partystarter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendeeRepository extends JpaRepository<Attendee, Long> {
    Optional<Attendee> findByEventIdAndUser(Integer eventId, User user);
    Optional<Attendee> findByEventIdAndGuest(Integer eventId, GuestUser guest);
    List<Attendee> findByEventIdOrderByCreatedAtAsc(Integer eventId);
    boolean existsByEventIdAndUser(Integer eventId, User user);
    boolean existsByEventIdAndGuest(Integer eventId, GuestUser guest);
}
