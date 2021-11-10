
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

    private List<Track> tracks = null;
    private List<Seed> seeds = null;

}
