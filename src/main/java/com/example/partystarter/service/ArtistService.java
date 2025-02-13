package com.example.partystarter.service;

import com.example.partystarter.model.Artist;
import com.example.partystarter.model.spotify.ArtistResponse;
import com.example.partystarter.repo.ArtistRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ArtistService {
    private final ArtistRepository artistRepository;

    public Set<Artist> getOrCreateArtists(List<ArtistResponse> artistResponses) {
        return artistResponses.stream()
                .map(this::getOrCreateArtist)
                .collect(Collectors.toSet());
    }

    private Artist getOrCreateArtist(ArtistResponse artistResponse) {
        return artistRepository.findById(artistResponse.getId())
                .orElseGet(() -> artistRepository.save(
                        Artist.builder()
                                .spotifyId(artistResponse.getId())
                                .name(artistResponse.getName())
                                .images(artistResponse.getImages())
                                .genres(new HashSet<>(artistResponse.getGenres()))
                                .spotifyUrl(artistResponse.getSpotifyUrl())
                                .build()
                ));
    }

} 