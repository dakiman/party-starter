package com.example.partystarter.model.cocktail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetDrinksByIngredientResponse {
    @JsonProperty("drinks")
    private List<SimpleDrink> drinks = null;
}
