package com.example.partystarter.service;

import com.example.partystarter.service.spotify.SpotifyCaller;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SpotifySongService {

    private final SpotifyCaller spotifyCaller;

    public Object getFeaturedPlaylists() {
        return spotifyCaller.getFeaturedPlaylists();
    }

}
