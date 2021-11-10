package com.example.partystarter.model.cocktail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetIngredientsResponse {
    @JsonProperty("drinks")
    private List<SimpleIngredient> ingredients = null;
}
