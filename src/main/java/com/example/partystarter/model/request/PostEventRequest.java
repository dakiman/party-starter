package com.example.partystarter.model.request;

import com.example.partystarter.model.spotify.ArtistResponse;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class PostEventRequest {
    @Length(min = 3, max = 40)
    private String name;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;
    
    private LocationRequest location;
    
    private List<ArtistResponse> artists;
    
    private List<Integer> drinks;
    
    private List<Integer> ingredients;
    
    private List<String> food;

    private Boolean isPrivate;

    @Data
    public static class LocationRequest {
        private Double lat;
        private Double lng;
        private String locationDescription;
    }
} 