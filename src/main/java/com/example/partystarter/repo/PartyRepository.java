package com.example.partystarter.repo;

import com.example.partystarter.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party, Integer> {
}
