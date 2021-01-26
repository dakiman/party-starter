package com.example.partystarter.service.cocktail;

import com.example.partystarter.model.cocktail.GetIngredientsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CocktailCaller {

    private final CocktailClient cocktailClient;

    @Autowired
    public CocktailCaller(CocktailClient cocktailClient) {
        this.cocktailClient = cocktailClient;
    }

    public GetIngredientsResponse getAllIngredients() {
        return cocktailClient.getAllIngredients();
    }

}
