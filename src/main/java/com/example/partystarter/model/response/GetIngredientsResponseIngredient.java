package com.example.partystarter.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetIngredientsResponseIngredient {
    private String name;
    private String description;
    private Boolean isAlcoholic;
    private String abv;
}
