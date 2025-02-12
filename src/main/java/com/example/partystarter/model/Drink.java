package com.example.partystarter.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Drink {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @Column(length = 10000)
    private String recipe;

    private int externalId;

    private Boolean isAlcoholic;

    private String thumbnail;

    @OneToMany(mappedBy = "drink", cascade = CascadeType.PERSIST)
    Set<DrinkIngredient> ingredients = new HashSet<>();
    //
    @ManyToMany(mappedBy = "drinks")
    private Set<Party> parties = new HashSet<>();
}
