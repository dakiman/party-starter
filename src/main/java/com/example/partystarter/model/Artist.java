package com.example.partystarter.model;

import com.example.partystarter.model.spotify.ImageResponse;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Artist {
    @Id
    private String spotifyId;
    private String name;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<ImageResponse> images;
    
    private String spotifyUrl;

    @ElementCollection
    @CollectionTable(name = "artist_genres", 
                    joinColumns = @JoinColumn(name = "artist_id"))
    @Column(name = "genre")
    @Builder.Default
    private Set<String> genres = new HashSet<>();

    @ManyToMany(mappedBy = "artists")
    @Builder.Default
    private Set<Event> events = new HashSet<>();
} 