package com.example.partystarter.model.cocktail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetDrinksByIngredientResponse {
    @JsonProperty("drinks")
    public List<SimpleDrink> drinks = null;
}
