package com.example.partystarter.service.lastfm;

import com.example.partystarter.model.lastfm.LastFmSimilarResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "lastfm", url = "${application.lastfm.url}")
public interface LastFmClient {

    @GetMapping("/2.0/")
    LastFmSimilarResponse getSimilarArtists(@RequestParam("method") String method,
                                            @RequestParam("artist") String artist,
                                            @RequestParam("api_key") String apiKey,
                                            @RequestParam("format") String format,
                                            @RequestParam("limit") int limit);
}
