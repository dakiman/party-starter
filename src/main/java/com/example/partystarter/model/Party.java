package com.example.partystarter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Party {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;
    private LocalDate date;
    private LocalTime time;

    @Embedded
    private Location location;

    @ManyToMany
    @JoinTable(name = "party_artists",
            joinColumns = @JoinColumn(name = "party_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id"))
    @Builder.Default
    private Set<Artist> artists = new HashSet<>();

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "party_drinks",
            joinColumns = @JoinColumn(name = "party_id"),
            inverseJoinColumns = @JoinColumn(name = "drink_id"))
    @Builder.Default
    private List<Drink> drinks = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "party_food_items", 
                    joinColumns = @JoinColumn(name = "party_id"))
    @Column(name = "food_item")
    @Builder.Default
    private List<String> foodItems = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
