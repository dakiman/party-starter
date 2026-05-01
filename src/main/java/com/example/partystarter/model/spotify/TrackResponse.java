package com.example.partystarter.model.spotify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackResponse {
    private String id;
    private String name;
    private String albumImageUrl;
    private Integer durationMs;
}
