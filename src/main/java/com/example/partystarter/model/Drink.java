package com.example.partystarter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
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
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // hide from json resposne
    private Integer id;

    private String name;

    @Column(length = 10000)
    private String recipe;

    private int externalId;

    private Boolean isAlcoholic;

    @OneToMany(mappedBy = "drink", cascade = CascadeType.PERSIST)
    Set<DrinkIngredient> ingredients = new HashSet<>();

}
