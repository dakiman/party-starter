package com.example.partystarter.service;

import com.example.partystarter.model.Ingredient;
import com.example.partystarter.model.cocktail.ExtendedIngredient;
import com.example.partystarter.model.cocktail.GetIngredientDetailsResponse;
import com.example.partystarter.model.cocktail.GetIngredientsResponse;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.service.cocktail.CocktailClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DrinksService {

    private final CocktailClient cocktailClient;
    private final IngredientRepository ingredientRepository;

    public void retrieveAndSaveIngredients() {
        GetIngredientsResponse response = cocktailClient.getAllIngredients();
        response.ingredients
                .forEach(ingredient -> {
                    GetIngredientDetailsResponse res = cocktailClient.getIngredientDetails(ingredient.getName());
                    res.getIngredients().forEach(this::saveNewIngredient);
                });
    }

//    TODO Handle duplicate ingredients
    private Ingredient saveNewIngredient(ExtendedIngredient ingredient) {
        Ingredient newIngredient = Ingredient.builder()
                .name(ingredient.getName())
                .abv(ingredient.getAbv())
                .description(ingredient.getDescription())
                .isAlcoholic(ingredient.getAlchohol() != null && ingredient.getAlchohol().equals("Yes"))
                .build();

        log.info("Saving new ingredient {}", newIngredient.getName());

        try {
            if(!ingredientRepository.existsByName(newIngredient.getName()))
                ingredientRepository.save(newIngredient);
            else
                log.error("Duplicate entry ignored for ingredient {}", ingredient.getName());
        } catch (Exception e) {
            log.error("Couldnt save ingredient {}. Message : \n {}", ingredient.getName(), e.getMessage());
        }

        return newIngredient;
    }

}
