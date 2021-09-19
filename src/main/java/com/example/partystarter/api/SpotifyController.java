package com.example.partystarter.api;

import com.example.partystarter.service.SpotifySongService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/spotify")
public class SpotifyController {

    private final SpotifySongService spotifySongService;

    @GetMapping(path = "")
    ResponseEntity getFeaturedPlaylists() {
        return ResponseEntity.ok(spotifySongService.getFeaturedPlaylists());
    }

}
