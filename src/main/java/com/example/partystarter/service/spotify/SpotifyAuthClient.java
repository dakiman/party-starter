package com.example.partystarter.service.spotify;

import com.example.partystarter.config.SpotifyAuthFeignConfig;
import com.example.partystarter.model.spotify.SpotifyTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@FeignClient(name = "spotifyAuth", url = "${application.spotify.auth.url}", path = "/api", configuration = {SpotifyAuthFeignConfig.class})
public interface SpotifyAuthClient {
    @PostMapping(value = "/token", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    SpotifyTokenResponse getSpotifyToken(@RequestHeader(value = "Authorization") String authorization,
                                         @RequestBody Map<String, ?> form);
}
