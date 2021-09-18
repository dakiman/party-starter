package com.example.partystarter.service.spotify;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "spotify", url = "${application.spotify.url}", path = "/v1")
public interface SpotifyClient {
    //        TODO Add interceptor to add token to all requests
    @GetMapping(value = "/tracks/{id}")
    Object getTrack(@PathVariable("id") String trackId);

    @GetMapping(value = "/browse/featured-playlists")
    Object getFeaturedPlaylists();
}