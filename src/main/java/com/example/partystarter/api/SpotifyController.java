package com.example.partystarter.api;

import com.example.partystarter.model.spotify.SpotifyGetGenresResponse;
import com.example.partystarter.service.SpotifySongService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/spotify")
public class SpotifyController {

    private final SpotifySongService spotifySongService;

    @GetMapping(path = "/genres")
    ResponseEntity<SpotifyGetGenresResponse> getFeaturedPlaylists() {
        return ResponseEntity.ok(spotifySongService.getGenres());
    }

    @GetMapping(path = "/recommendations")
    ResponseEntity<?> getRecommendations(@RequestParam List<String> genres, @RequestParam List<String> types) {
        Object recommendations = spotifySongService.getRecommendations(genres, types);
        return ResponseEntity.ok(recommendations);
    }

}
