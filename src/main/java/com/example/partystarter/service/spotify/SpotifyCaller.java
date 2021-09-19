package com.example.partystarter.service.spotify;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SpotifyCaller {
    private final SpotifyClient spotifyClient;

    public Object getTrack(String trackId) {
        return spotifyClient.getTrack(trackId);
    }

    public Object getFeaturedPlaylists() {
        return spotifyClient.getFeaturedPlaylists();
    }
}
