package com.example.partystarter.repo;

import com.example.partystarter.model.Event;
import com.example.partystarter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByCreatorOrderByCreatedAtDesc(User creator);
} 