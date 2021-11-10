
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
public class Album {

    @JsonProperty("album_type")
    public String albumType;
    @JsonProperty("artists")
    public List<Artist> artists = null;
    @JsonProperty("available_markets")
    public List<Object> availableMarkets = null;
    @JsonProperty("external_urls")
    public ExternalUrls externalUrls;
    public String href;
    public String id;
    public List<Image> images = null;
    public String name;
    @JsonProperty("release_date")
    public String releaseDate;
    @JsonProperty("release_date_precision")
    public String releaseDatePrecision;
    @JsonProperty("total_tracks")
    public Integer totalTracks;
    public String type;
    public String uri;

}
