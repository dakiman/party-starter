package com.example.partystarter.service.cocktail;

import com.example.partystarter.model.cocktail.GetDrinkByIdResponse;
import com.example.partystarter.model.cocktail.GetDrinksByIngredientResponse;
import com.example.partystarter.model.cocktail.GetIngredientDetailsResponse;
import com.example.partystarter.model.cocktail.GetIngredientsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//9973533
@FeignClient(name = "cocktailDb", url = "https://www.thecocktaildb.com", path = "/api/json/v2/9973533")
public interface CocktailClient {

    @GetMapping(value = "/list.php?i=list")
    GetIngredientsResponse getAllIngredients();

    @GetMapping(value = "/search.php")
    GetIngredientDetailsResponse getIngredientDetails(@RequestParam(value = "i") String ingredient);

    @GetMapping(value = "/filter.php")
    GetDrinksByIngredientResponse getCocktailsByIngredient(@RequestParam("i") String ingredient);

    @GetMapping(value = "/lookup.php")
    GetDrinkByIdResponse getCocktailById(@RequestParam(value = "i") String id);

}
