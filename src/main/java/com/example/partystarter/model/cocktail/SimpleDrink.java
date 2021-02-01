package com.example.partystarter.model.cocktail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SimpleDrink {
    @JsonProperty("idDrink")
    public String id;

    @JsonProperty("strDrink")
    public String name;
}
