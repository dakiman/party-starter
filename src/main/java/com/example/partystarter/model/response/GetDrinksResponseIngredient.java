package com.example.partystarter.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetDrinksResponseIngredient {
    private String name;
    //    private String description;
    private String abv;
    private Boolean isAlcoholic;
    private String amount;
}
