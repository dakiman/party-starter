package com.example.partystarter.model.cocktail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SimpleIngredient {
    @JsonProperty("strIngredient1")
    String name;
}
