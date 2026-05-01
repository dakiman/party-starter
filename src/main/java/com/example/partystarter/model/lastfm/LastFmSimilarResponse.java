package com.example.partystarter.model.lastfm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastFmSimilarResponse {

    @JsonProperty("similarartists")
    private SimilarArtists similarArtists;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimilarArtists {
        @JsonProperty("artist")
        private List<LastFmArtist> artists;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastFmArtist {
        private String name;
        private String match;
    }
}
