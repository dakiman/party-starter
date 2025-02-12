package com.example.partystarter.service.spotify;

import com.example.partystarter.config.SpotifyTokenFeignConfig;
import com.example.partystarter.model.spotify.GetRecommendationsResponse;
import com.example.partystarter.model.spotify.SpotifyGetGenresResponse;
import com.example.partystarter.model.spotify.SpotifySearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "spotify", url = "${application.spotify.url}", path = "/v1", configuration = {SpotifyTokenFeignConfig.class})
public interface SpotifyClient {
    @GetMapping(value = "/tracks/{id}")
    Object getTrack(@PathVariable("id") String trackId);

    @GetMapping(value = "/browse/featured-playlists")
    Object getFeaturedPlaylists();

    @GetMapping(value = "/recommendations/available-genre-seeds")
    SpotifyGetGenresResponse getGenres();

    @GetMapping(value = "/recommendations")
    GetRecommendationsResponse getRecommendations(@RequestParam(value = "seed_genres") List<String> genres, @RequestParam(value = "type") List<String> types);

    @GetMapping(value = "/search")
    SpotifySearchResponse searchArtists(@RequestParam("q") String query,
                                      @RequestParam("type") String type,
                                      @RequestParam("limit") int limit);
}
