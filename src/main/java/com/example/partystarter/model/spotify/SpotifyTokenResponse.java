package com.example.partystarter.model.spotify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyTokenResponse {
    private String access_token;
    private String token_type;
    private Integer expires_in;
}
