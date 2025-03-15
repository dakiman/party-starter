package com.example.partystarter.api;

import com.example.partystarter.model.spotify.SearchArtistsResponse;
import com.example.partystarter.model.spotify.SpotifyGetGenresResponse;
import com.example.partystarter.service.SpotifySongService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/music")
public class MusicController {

    private final SpotifySongService spotifySongService;

    @GetMapping("/genres")
    public ResponseEntity<SpotifyGetGenresResponse> getFeaturedPlaylists() {
        return ResponseEntity.ok(spotifySongService.getGenres());
    }

//    @GetMapping(path = "/recommendations")
//    public ResponseEntity<Object> getRecommendations(@RequestParam List<String> genres, @RequestParam List<String> types) {
//        Object recommendations = spotifySongService.getRecommendations(genres, types);
//        return ResponseEntity.ok(recommendations);
//    }

    @GetMapping("/artists")
    public ResponseEntity<SearchArtistsResponse> searchArtists(@RequestParam String name) {
        return ResponseEntity.ok(spotifySongService.searchArtists(name));
    }

}
