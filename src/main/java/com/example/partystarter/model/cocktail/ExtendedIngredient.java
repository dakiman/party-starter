package com.example.partystarter.model.cocktail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExtendedIngredient {

    @JsonProperty("idIngredient")
    private String id;
    @JsonProperty("strIngredient")
    private String name;
    @JsonProperty("strDescription")
    private String description;
    @JsonProperty("strType")
    private String type;
    @JsonProperty("strAlcohol")
    private String alchohol;
    @JsonProperty("strABV")
    private String abv;

}
