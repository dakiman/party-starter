
package com.example.partystarter.model.spotify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seed {

    private Integer initialPoolSize;
    private Integer afterFilteringSize;
    private Integer afterRelinkingSize;
    private String id;
    private String type;
    private Object href;

}
