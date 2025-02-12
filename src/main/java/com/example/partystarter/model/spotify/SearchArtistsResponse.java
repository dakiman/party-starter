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
public class SearchArtistsResponse {
    private List<ArtistResponse> artists;
    private int total;
} 