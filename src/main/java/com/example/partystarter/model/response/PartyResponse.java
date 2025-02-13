package com.example.partystarter.model.response;

import com.example.partystarter.model.spotify.ImageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyResponse {
    private Integer id;
    private String name;
    private LocalDate date;
    private LocalTime time;
    private LocationResponse location;
    private List<ArtistResponse> artists;
    private List<GetDrinksResponseDrink> drinks;
    private List<String> food;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationResponse {
        private Double latitude;
        private Double longitude;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArtistResponse {
        private String spotifyId;
        private String name;
        private List<ImageResponse> images;
        private List<String> genres;
        private String spotifyUrl;
    }
}
