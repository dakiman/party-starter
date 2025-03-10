package com.example.partystarter.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;

    @Embedded
    private Location location;

    @ManyToMany
    @JoinTable(name = "event_artists",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id"))
    @Builder.Default
    private Set<Artist> artists = new HashSet<>();

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "event_drinks",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "drink_id"))
    @Builder.Default
    private List<Drink> drinks = new ArrayList<>();

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "event_ingredients",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id"))
    @Builder.Default
    private List<Ingredient> ingredients = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "event_food_items", 
                    joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "food_item")
    @Builder.Default
    private List<String> foodItems = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    private Boolean isPrivate = false;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;
} 