package com.example.partystarter.service;

import com.example.partystarter.model.spotify.SpotifyGetGenresResponse;
import com.example.partystarter.model.spotify.SearchArtistsResponse;
import com.example.partystarter.service.spotify.SpotifyCaller;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SpotifySongService {

    private final SpotifyCaller spotifyCaller;

    public Object getFeaturedPlaylists() {
        return spotifyCaller.getFeaturedPlaylists();
    }

    @Cacheable(cacheNames = "genres")
    public SpotifyGetGenresResponse getGenres() {
        return spotifyCaller.getGenres();
    }

    public Object getRecommendations(List<String> genres, List<String> types) {
        return spotifyCaller.getRecommendations(genres, types);
    }

    public SearchArtistsResponse searchArtists(String name) {
        return spotifyCaller.searchArtists(name);
    }

}
