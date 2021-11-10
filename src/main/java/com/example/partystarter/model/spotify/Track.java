
package com.example.partystarter.model.spotify;

import java.util.List;
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
public class Track {

    public Album album;
    public List<Artist> artists = null;
    @JsonProperty("available_markets")
    public List<Object> availableMarkets = null;
    @JsonProperty("disc_number")
    public Integer discNumber;
    @JsonProperty("duration_ms")
    public Integer durationMs;
    public Boolean explicit;
    @JsonProperty("external_ids")
    public ExternalIds externalIds;
    @JsonProperty("external_urls")
    public ExternalUrls externalUrls;
    public String href;
    public String id;
    @JsonProperty("is_local")
    public Boolean isLocal;
    public String name;
    public Integer popularity;
    @JsonProperty("preview_url")
    public Object previewUrl;
    @JsonProperty("track_number")
    public Integer trackNumber;
    public String type;
    public String uri;

}
