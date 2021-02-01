package com.example.partystarter.service.cocktail;

import com.example.partystarter.model.cocktail.GetDrinkByIdResponse;
import com.example.partystarter.model.cocktail.GetDrinksByIngredientResponse;
import com.example.partystarter.model.cocktail.GetIngredientDetailsResponse;
import com.example.partystarter.model.cocktail.GetIngredientsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CocktailCaller {

    private final CocktailClient cocktailClient;

    @Autowired
    public CocktailCaller(CocktailClient cocktailClient) {
        this.cocktailClient = cocktailClient;
    }

    public GetIngredientsResponse getAllIngredients() {
        return cocktailClient.getAllIngredients();
    }

    public GetDrinkByIdResponse getDrinkById(String id) {
        GetDrinkByIdResponse cocktailById = null;
        try {
            cocktailById = cocktailClient.getCocktailById(id);
        } catch (Exception e) {
            log.info("Exception thrown while retrieving cocktail by ID", e);
        }
        return cocktailById;
    }

    public GetDrinksByIngredientResponse getDrinkByIngredient(String ingredient) {
            GetDrinksByIngredientResponse cocktailsByIngredient = null;
        try {
            cocktailsByIngredient = cocktailClient.getCocktailsByIngredient(ingredient);
        } catch (Exception e) {
            log.info("Exception thrown while retrieving cocktail by Ingredient", e);
        }
        return cocktailsByIngredient;
    }

    public GetIngredientDetailsResponse getIngredientDetails(String id) {
        return cocktailClient.getIngredientDetails(id);
    }

}
