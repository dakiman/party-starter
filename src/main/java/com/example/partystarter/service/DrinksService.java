package com.example.partystarter.service;

import com.example.partystarter.model.Ingredient;
import com.example.partystarter.model.cocktail.ExtendedIngredient;
import com.example.partystarter.model.cocktail.GetIngredientDetailsResponse;
import com.example.partystarter.model.cocktail.GetIngredientsResponse;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.service.cocktail.CocktailClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DrinksService {

    private final CocktailClient cocktailClient;
    private final IngredientRepository ingredientRepository;

    public DrinksService(CocktailClient cocktailClient, IngredientRepository ingredientRepository) {
        this.cocktailClient = cocktailClient;
        this.ingredientRepository = ingredientRepository;
    }

    public void retrieveAndSaveIngredients() {
        GetIngredientsResponse response = cocktailClient.getAllIngredients();

        response
                .ingredients
                .forEach(ingredient -> {
                    GetIngredientDetailsResponse res = cocktailClient.getIngredientDetails(ingredient.getName());

                    res.getIngredients().forEach(ingredientData -> {

                        Ingredient newIngredient = Ingredient.builder()
                                .name(ingredientData.getName())
                                .abv(ingredientData.getAbv())
                                .description(ingredientData.getDescription())
                                .isAlcoholic(ingredientData.getAlchohol() != null && ingredientData.getAlchohol().equals("Yes"))
                                .build();

                        log.info("Saving new ingredient {}", newIngredient);
                        ingredientRepository.save(newIngredient);
                    });

                });
    }
}
