package com.example.partystarter.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
// Explore why no args constructor is required here but not in Drink entity
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @Column(length = 10000)
    private String description;

    private String abv;

    private Boolean isAlcoholic;

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.PERSIST)
    Set<DrinkIngredient> drinks = new HashSet<>();

}
