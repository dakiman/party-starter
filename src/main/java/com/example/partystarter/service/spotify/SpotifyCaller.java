package com.example.partystarter.service.spotify;

import com.example.partystarter.model.spotify.GetRecommendationsResponse;
import com.example.partystarter.model.spotify.SpotifyGetGenresResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SpotifyCaller {
    private final SpotifyClient spotifyClient;

    public Object getTrack(String trackId) {
        return spotifyClient.getTrack(trackId);
    }

    public Object getFeaturedPlaylists() {
        return spotifyClient.getFeaturedPlaylists();
    }

    public SpotifyGetGenresResponse getGenres() {
        return spotifyClient.getGenres();
    }

    public Object getRecommendations(List<String> genres, List<String> types) {
        GetRecommendationsResponse recommendations = spotifyClient.getRecommendations(genres, types);
        log.info("Artist:" + recommendations.getTracks().get(0).getArtists().get(0).getName());
        return recommendations.getTracks().get(0).getArtists().get(0).getExternalUrls().getSpotify();
    }

}
