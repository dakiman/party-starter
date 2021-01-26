package com.example.partystarter.model.cocktail;

import lombok.Data;

import java.util.List;

@Data
public class GetIngredientDetailsResponse {
    List<ExtendedIngredient> ingredients = null;
}
