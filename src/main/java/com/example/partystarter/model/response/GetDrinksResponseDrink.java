package com.example.partystarter.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GetDrinksResponseDrink {
    private String name;
    private String recipe;
    private boolean isAlcoholic;
    private List<GetDrinksResponseIngredient> ingredients;
}
