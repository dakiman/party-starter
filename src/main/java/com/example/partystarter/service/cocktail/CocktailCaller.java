package com.example.partystarter.service.cocktail;

import com.example.partystarter.model.cocktail.GetDrinkByIdResponse;
import com.example.partystarter.model.cocktail.GetDrinksByIngredientResponse;
import com.example.partystarter.model.cocktail.GetIngredientDetailsResponse;
import com.example.partystarter.model.cocktail.GetIngredientsResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class CocktailCaller {

    private final CocktailClient cocktailClient;

    public GetIngredientsResponse getAllIngredients() {
        return cocktailClient.getAllIngredients();
    }

    /*
     * The API has inconsistent data structure, returns empty string for non existent drink (With 200 OK)
     * */
    public GetDrinkByIdResponse getDrinkById(String id) {
        GetDrinkByIdResponse cocktailById = null;
        try {
            cocktailById = cocktailClient.getCocktailById(id);
        } catch (Exception e) {
            log.error("Exception thrown while retrieving cocktail by ID: {}", id);
            log.error("Exception : {}", e.getMessage());
        }
        return cocktailById;
    }

    /*
     * The API has inconsistent data structure, returns empty string for non existent ingredient (With 200 OK)
     * */
    public GetDrinksByIngredientResponse getDrinkByIngredient(String ingredient) {
        GetDrinksByIngredientResponse cocktailsByIngredient = null;
        try {
            cocktailsByIngredient = cocktailClient.getCocktailsByIngredient(ingredient);
        } catch (Exception e) {
            log.error("Exception thrown while retrieving cocktail by ingredient: {}", ingredient);
            log.error("Exception : {}", e.getMessage());
        }
        return cocktailsByIngredient;
    }

    public GetIngredientDetailsResponse getIngredientDetails(String id) {
        return cocktailClient.getIngredientDetails(id);
    }

}
