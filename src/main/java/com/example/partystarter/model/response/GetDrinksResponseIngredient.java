package com.example.partystarter.model.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GetDrinksResponseIngredient {
    private String name;
    //    private String description;
    private String abv;
    private Boolean isAlcoholic;
    private String amount;
}
