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
    private Integer id;
    private String name;
    private String recipe;
    private Boolean isAlcoholic;
    private String thumbnail;
    private List<GetDrinksResponseIngredient> ingredients;

    /** Empty list when {@link #fullyMakeable} is true; populated only by the
     * suggestions endpoint (GET /drinks?ingredients=...). The bare GET /drinks
     * leaves this null. */
    private List<String> missingAlcoholicIngredients;

    /** True iff every alcoholic ingredient required by this drink is in the
     * caller's picked set. False when missing alcoholics exist. Default false
     * for non-suggestion responses. */
    private boolean fullyMakeable;
}
