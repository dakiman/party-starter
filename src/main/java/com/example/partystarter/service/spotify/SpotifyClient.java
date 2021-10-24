package com.example.partystarter.service.spotify;

import com.example.partystarter.config.SpotifyTokenFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "spotify", url = "${application.spotify.url}", path = "/v1", configuration = {SpotifyTokenFeignConfig.class})
public interface SpotifyClient {
    @GetMapping(value = "/tracks/{id}")
    Object getTrack(@PathVariable("id") String trackId);

    @GetMapping(value = "/browse/featured-playlists")
    Object getFeaturedPlaylists();
}
