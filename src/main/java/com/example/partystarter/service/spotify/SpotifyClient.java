package com.example.partystarter.service.spotify;

import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "spotify", url = "${application.spotify.url}", path = "/v1")
public interface SpotifyClient {
    //        TODO Add interceptor to add token to all requests
    @GetMapping(value = "/tracks/{id}")
    Object getTrack(@Param("id") String trackId);
}
