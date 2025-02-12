package com.example.partystarter.model.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SpotifySearchResponse {
    @JsonProperty("artists")
    private SpotifyArtistsPage artists;

    @Data
    public static class SpotifyArtistsPage {
        private SpotifyArtist[] items;
        private int total;
    }
} 