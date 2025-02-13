package com.example.partystarter.repo;

import com.example.partystarter.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, String> {
} 