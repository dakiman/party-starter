package com.example.partystarter.service;

import com.example.partystarter.model.Ingredient;
import com.example.partystarter.model.cocktail.ExtendedIngredient;
import com.example.partystarter.model.cocktail.GetIngredientDetailsResponse;
import com.example.partystarter.model.cocktail.GetIngredientsResponse;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.service.cocktail.CocktailCaller;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class IngredientsService {
    private final CocktailCaller cocktailCaller;
    private final IngredientRepository ingredientRepository;

    // TODO improve foreach>
    public void retrieveAndSaveIngredients() {
        GetIngredientsResponse response = cocktailCaller.getAllIngredients();
        response.ingredients
                .forEach(ingredient -> {
                    if (!ingredientRepository.existsByName(ingredient.getName())) {
                        GetIngredientDetailsResponse res = cocktailCaller.getIngredientDetails(ingredient.getName());
                        res.getIngredients().forEach(this::saveNewIngredient);
                    }
                });
    }

    //    TODO Move mapping to separate method?
    private void saveNewIngredient(ExtendedIngredient ingredient) {
        try {
            if (!ingredientRepository.existsByName(ingredient.getName())) {
                log.info("Saving new ingredient {}", ingredient.getName());

                Ingredient newIngredient = Ingredient.builder()
                        .name(ingredient.getName())
                        .abv(ingredient.getAbv())
                        .description(ingredient.getDescription())
                        .isAlcoholic(ingredient.getAlchohol() != null && ingredient.getAlchohol().equals("Yes"))
                        .build();

                ingredientRepository.save(newIngredient);
            } else
                log.error("Duplicate entry ignored for ingredient {}", ingredient.getName());
        } catch (Exception e) {
            log.error("Couldnt save ingredient {}. Message : \n {}", ingredient.getName(), e.getMessage());
        }
    }

}
