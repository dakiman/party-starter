package com.example.partystarter.model.spotify;

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
public class SpotifyArtist {
    @JsonProperty("external_urls")
    private ExternalUrls externalUrls;
    private Followers followers;
    private List<String> genres;
    private String href;
    private String id;
    private List<Image> images;
    private String name;
    private int popularity;
    private String type;
    private String uri;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Followers {
        private String href;
        private Integer total;
    }
} 