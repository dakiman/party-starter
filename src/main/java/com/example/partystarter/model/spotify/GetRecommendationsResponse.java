package com.example.partystarter.model.spotify;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetRecommendationsResponse {

    @Builder.Default
    private List<Track> tracks = null;
    @Builder.Default
    private List<Seed> seeds = null;

}
