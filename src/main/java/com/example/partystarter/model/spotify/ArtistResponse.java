package com.example.partystarter.model.spotify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistResponse {
    private String id;
    private String name;
    private List<ImageResponse> images;
    private List<String> genres;
    private int followers;
    private int popularity;
    private String spotifyUrl;  // The external URL to open in Spotify
} 