package com.example.partystarter.service;

import com.example.partystarter.model.Drink;
import com.example.partystarter.model.DrinkIngredient;
import com.example.partystarter.model.Ingredient;
import com.example.partystarter.model.cocktail.*;
import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.model.response.GetDrinksResponseDrink;
import com.example.partystarter.model.response.GetDrinksResponseIngredient;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.service.cocktail.CocktailCaller;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.partystarter.utils.ReflectionUtil.getFieldValue;

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
                    if (!ingredientRepository.existsByName(ingredient.getName())) {
                        GetIngredientDetailsResponse res = cocktailCaller.getIngredientDetails(ingredient.getName());
                        res.getIngredients().forEach(this::saveNewIngredient);
                    }
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

    public GetDrinksResponse getDrinksForIngredients(List<String> ingredientNames) {
        List<Drink> drinks = drinkRepository.findDistinctByIngredientsIngredientNameIn(ingredientNames);

        List<GetDrinksResponseDrink> responseDrinks = drinks.stream().map(drink -> GetDrinksResponseDrink.builder()
                .isAlcoholic(drink.getIsAlcoholic())
                .name(drink.getName())
                .recipe(drink.getRecipe())
                .ingredients(mapIngredients(drink.getIngredients()))
                .build()).collect(Collectors.toList());

        return new GetDrinksResponse(responseDrinks);
    }

    private List<GetDrinksResponseIngredient> mapIngredients(Set<DrinkIngredient> ingredients) {
        return ingredients.stream().map(ingredient -> GetDrinksResponseIngredient.builder()
                .name(ingredient.getIngredient().getName())
//                .description(ingredient.getIngredient().getDescription())
                .isAlcoholic(ingredient.getIngredient().getIsAlcoholic())
                .abv(ingredient.getIngredient().getAbv())
                .amount(ingredient.getAmount())
                .build()).collect(Collectors.toList());
    }

    private void saveNewDrink(ExtendedDrink drink) {
        try {
            if (!drinkRepository.existsByName(drink.getStrDrink())) {
                log.info("Saving new Drink with name {}", drink.getStrDrink());

                Drink newDrink = Drink.builder()
                        .name(drink.getStrDrink())
                        .recipe(drink.getStrInstructions())
                        .externalId(Integer.parseInt(drink.getIdDrink()))
                        .isAlcoholic(drink.getStrAlcoholic().equals("Alcoholic"))
                        .build();

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
            } else
                log.error("Duplicate entry ignored for drink {}", drink.getStrDrink());
        } catch (Exception e) {
            log.error("Couldnt save drink {}. Message : \n {}", drink.getStrDrink(), e.getMessage());
        }
    }

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

    private Map<String, String> getIngredientsAndAmounts(ExtendedDrink drink) {
        Map<String, String> ingredientAmounts = new HashMap<>();

        for (int i = 1; i < 15; i++) {
            String name = getFieldValue(drink, "strIngredient" + i);
            String amount = getFieldValue(drink, "strMeasure" + i);

            if (name == null && amount == null)
                break;

            ingredientAmounts.put(name, amount);
        }

        return ingredientAmounts;
    }


}
