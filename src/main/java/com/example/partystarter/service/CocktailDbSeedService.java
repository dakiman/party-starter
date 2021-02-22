package com.example.partystarter.service;

import com.example.partystarter.model.Drink;
import com.example.partystarter.model.DrinkIngredient;
import com.example.partystarter.model.Ingredient;
import com.example.partystarter.model.cocktail.*;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.service.cocktail.CocktailCaller;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.partystarter.utils.ConvertUtils.*;

@Slf4j
@Service
@AllArgsConstructor
public class CocktailDbSeedService {

    private final CocktailCaller cocktailCaller;
    private final IngredientRepository ingredientRepository;
    private final DrinkRepository drinkRepository;

    public void retrieveAndSaveIngredients() {
        GetIngredientsResponse response = cocktailCaller.getAllIngredients();
        response.ingredients
                .forEach(ingredient -> {
                    GetIngredientDetailsResponse res = cocktailCaller.getIngredientDetails(ingredient.getName());
                    res.getIngredients().forEach(this::saveNewIngredient);
                });
    }

    public void retrieveDrinksForAllIngredients() {
        List<Ingredient> ingredients = ingredientRepository.findAll();

        ingredients.forEach(ingredient -> {
            log.info("Retrieving drinks for ingredient {}", ingredient.getName());
            GetDrinksByIngredientResponse res = cocktailCaller.getDrinkByIngredient(ingredient.getName());

            if (res == null) return;

            res.drinks.forEach(drink -> {
                log.info("Retrieving data for drink {}", drink.getName());
                GetDrinkByIdResponse drinkByIdResponse = cocktailCaller.getDrinkById(drink.getId());

                drinkByIdResponse.getDrinks().forEach(this::saveNewDrink);
            });
        });
    }

    private void saveNewIngredient(ExtendedIngredient ingredient) {
        if (ingredientRepository.existsByName(ingredient.getName())) {
            log.error("Duplicate entry ignored for ingredient {}", ingredient.getName());
            return;
        }

        log.info("Saving new ingredient {}", ingredient.getName());
        Ingredient newIngredient = mapIngredient(ingredient);
        ingredientRepository.save(newIngredient);
    }

    private void saveNewDrink(ExtendedDrink drink) {
        if (drinkRepository.existsByName(drink.getStrDrink())) {
            log.error("Duplicate entry ignored for drink {}", drink.getStrDrink());
            return;
        }

        log.info("Saving new Drink with name {}", drink.getStrDrink());
        Drink newDrink = mapDrink(drink);

        Map<String, String> ingredientAmounts = getIngredientsAndAmounts(drink);
        Set<DrinkIngredient> drinkIngredients = new HashSet<>();

        ingredientAmounts.forEach((ingredientName, amount) -> {
            Ingredient ingredient = ingredientRepository
                    .getByName(ingredientName)
                    .orElseGet(() ->
                            ingredientRepository.save(Ingredient.builder()
                                    .name(ingredientName)
                                    .build())
                    );

            DrinkIngredient drinkIngredient = DrinkIngredient.builder()
                    .amount(amount)
                    .drink(newDrink)
                    .ingredient(ingredient)
                    .build();

            drinkIngredients.add(drinkIngredient);
        });

        newDrink.setIngredients(drinkIngredients);
        drinkRepository.save(newDrink);
    }

}
