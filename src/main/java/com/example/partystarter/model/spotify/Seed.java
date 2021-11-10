
package com.example.partystarter.model.spotify;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seed {

    public Integer initialPoolSize;
    public Integer afterFilteringSize;
    public Integer afterRelinkingSize;
    public String id;
    public String type;
    public Object href;

}
