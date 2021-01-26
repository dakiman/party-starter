package com.example.partystarter.api;

import com.example.partystarter.model.Drink;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.service.DrinksService;
import com.example.partystarter.service.cocktail.CocktailCaller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/drinks")
public class DrinksController {

    private final DrinkRepository drinkRepository;
    private final IngredientRepository ingredientRepository;
    private final CocktailCaller cocktailCaller;
    private final DrinksService drinksService;

    public DrinksController(DrinkRepository drinkRepository, IngredientRepository ingredientRepository, CocktailCaller cocktailCaller, DrinksService drinksService) {
        this.drinkRepository = drinkRepository;
        this.ingredientRepository = ingredientRepository;
        this.cocktailCaller = cocktailCaller;
        this.drinksService = drinksService;
    }

    @GetMapping
    Iterable<Drink> getDrinks() {
        return drinkRepository.findAll();
    }

    @GetMapping(path = "/ingredients")
    ResponseEntity getIngredients() {
        return ResponseEntity.ok(cocktailCaller.getAllIngredients());
    }

//    @GetMapping(path = "/ingredients/sync")
//    ResponseEntity syncIngredients() {
//        drinksService.retrieveAndSaveIngredients();
//        return ResponseEntity.ok(ingredientRepository.findAll());
//    }


}
