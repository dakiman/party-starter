package com.example.partystarter.service.cocktail;

import com.example.partystarter.model.cocktail.GetIngredientDetailsResponse;
import com.example.partystarter.model.cocktail.GetIngredientsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "cocktailDb", url = "https://www.thecocktaildb.com", path = "/api/json/v2/9973533")
public interface CocktailClient {

    @GetMapping(value = "/list.php?i=list")
    GetIngredientsResponse getAllIngredients();

    @GetMapping(value = "/search.php")
    GetIngredientDetailsResponse getIngredientDetails(@RequestParam(value = "i") String ingredient);

}
