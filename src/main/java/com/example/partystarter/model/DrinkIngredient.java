package com.example.partystarter.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DrinkIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "drink_id")
    Drink drink;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    Ingredient ingredient;

    String amount;
}
