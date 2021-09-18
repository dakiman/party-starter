package com.example.partystarter.service.spotify;

import com.example.partystarter.model.spotify.SpotifyTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "spotifyAuth", url = "${application.spotify.auth.url}", path = "/api")
public interface SpotifyAuthClient {
    @PostMapping(value = "/token")
    SpotifyTokenResponse getSpotifyToken(@RequestHeader(value = "Authorization") String authorization);
}
