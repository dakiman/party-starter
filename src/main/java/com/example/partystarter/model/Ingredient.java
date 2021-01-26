package com.example.partystarter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Data
// Explore why no args constructor is required here but not in Drink entity
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // hide from json resposne
    private Integer id;

    private String name;

    @Column(length = 10000)
    private String description;

    private String abv;

    private Boolean isAlcoholic;
}