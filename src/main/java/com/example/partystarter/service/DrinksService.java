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

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class DrinksService {

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

    private Drink saveNewDrink(ExtendedDrink drink) {
        log.info("Saving new Drink with name {}", drink.getStrDrink());

        Drink newDrink = Drink.builder()
                .name(drink.getStrDrink())
                .recipe(drink.getStrInstructions())
                .externalId(Integer.parseInt(drink.getIdDrink()))
                .isAlcoholic(drink.getStrAlcoholic().equals("Alcoholic"))
                .build();

        DrinkIngredient drinkIngredient = DrinkIngredient.builder()
                .amount("1/2")
                .drink(newDrink)
                .ingredient(ingredientRepository.getByName("Gin"))
                .build();

        newDrink.setIngredients(Set.of(drinkIngredient));

        try {
            if (!drinkRepository.existsByName(newDrink.getName()))
                drinkRepository.save(newDrink);
            else
                log.error("Duplicate entry ignored for drink {}", newDrink.getName());
        } catch (Exception e) {
            log.error("Couldnt save drink {}. Message : \n {}", newDrink.getName(), e.getMessage());
        }

        return newDrink;
    }

    private Ingredient saveNewIngredient(ExtendedIngredient ingredient) {
        Ingredient newIngredient = Ingredient.builder()
                .name(ingredient.getName())
                .abv(ingredient.getAbv())
                .description(ingredient.getDescription())
                .isAlcoholic(ingredient.getAlchohol() != null && ingredient.getAlchohol().equals("Yes"))
                .build();

        log.info("Saving new ingredient {}", newIngredient.getName());

        try {
            if (!ingredientRepository.existsByName(newIngredient.getName()))
                ingredientRepository.save(newIngredient);
            else
                log.error("Duplicate entry ignored for ingredient {}", ingredient.getName());
        } catch (Exception e) {
            log.error("Couldnt save ingredient {}. Message : \n {}", ingredient.getName(), e.getMessage());
        }

        return newIngredient;
    }


}
