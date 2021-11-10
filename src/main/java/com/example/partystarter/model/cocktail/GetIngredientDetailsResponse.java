package com.example.partystarter.model.cocktail;

import lombok.Data;

import java.util.List;

@Data
public class GetIngredientDetailsResponse {
    private List<ExtendedIngredient> ingredients = null;
}
