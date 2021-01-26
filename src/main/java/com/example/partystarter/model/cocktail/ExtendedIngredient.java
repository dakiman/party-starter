package com.example.partystarter.model.cocktail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExtendedIngredient {

    @JsonProperty("idIngredient")
    public String id;
    @JsonProperty("strIngredient")
    public String name;
    @JsonProperty("strDescription")
    public String description;
    @JsonProperty("strType")
    public String type;
    @JsonProperty("strAlcohol")
    public String alchohol;
    @JsonProperty("strABV")
    public String abv;

}
