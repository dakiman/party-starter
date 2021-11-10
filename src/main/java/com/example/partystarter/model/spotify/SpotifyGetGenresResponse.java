package com.example.partystarter.model.spotify;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyGetGenresResponse {
    private List<String> genres;
}
