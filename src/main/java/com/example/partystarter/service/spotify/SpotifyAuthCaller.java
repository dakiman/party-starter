package com.example.partystarter.service.spotify;

import com.example.partystarter.model.spotify.SpotifyTokenResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class SpotifyAuthCaller {
    private final SpotifyAuthClient spotifyAuthClient;

    public SpotifyTokenResponse getSpotifyToken(String authToken) {
        return spotifyAuthClient.getSpotifyToken(authToken);
    }

}
