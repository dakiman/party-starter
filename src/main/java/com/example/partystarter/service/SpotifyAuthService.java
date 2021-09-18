package com.example.partystarter.service;

import com.example.partystarter.model.spotify.SpotifyTokenResponse;
import com.example.partystarter.service.spotify.SpotifyAuthCaller;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@AllArgsConstructor
public class SpotifyAuthService {
    private final SpotifyAuthCaller spotifyAuthCaller;
    @Value("${application.spotify.client-id}")
    private String clientId;
    @Value("${application.spotify.client-secret}")
    private String clientSecret;

//    TODO cache token on method level???
    public String getSpotifyToken() {
        String authSecretData = clientId + ":" + clientSecret;
        String authHeader = Base64.getEncoder().encodeToString(authSecretData.getBytes());
        SpotifyTokenResponse tokenResponse = spotifyAuthCaller.getSpotifyToken(authHeader);
//        TODO change snakecase to camel case if possible
        return tokenResponse.getAccess_token();
    }
}
