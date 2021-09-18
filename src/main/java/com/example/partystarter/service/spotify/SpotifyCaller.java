package com.example.partystarter.service.spotify;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SpotifyCaller {
    private final SpotifyCaller spotifyCaller;

    public Object getTrack(String trackId) {
        return spotifyCaller.getTrack(trackId);
    }

    public Object getFeaturedPlaylists() {
        return spotifyCaller.getFeaturedPlaylists();
    }
}
