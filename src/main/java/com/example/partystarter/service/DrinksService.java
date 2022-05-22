package com.example.partystarter.service;

import com.example.partystarter.model.Drink;
import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.model.response.GetDrinksResponseDrink;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;


@Slf4j
@Service
@AllArgsConstructor
public class DrinksService {

    private final DrinkRepository drinkRepository;

    @Cacheable(cacheNames = "drinks")
    public GetDrinksResponse getDrinksForIngredients(List<String> ingredientNames) {
        List<GetDrinksResponseDrink> responseDrinks = drinkRepository
                .findDistinctByIngredientsIngredientNameIn(ingredientNames)
                .stream()
                .map(ConvertUtils::mapDrinksToResponse)
                .toList();

        return new GetDrinksResponse(responseDrinks);
    }

    public GetDrinksResponse getAllDrinks() {
        List<GetDrinksResponseDrink> responseDrinks = drinkRepository
                .findAll()
                .stream()
                .map(ConvertUtils::mapDrinksToResponse)
                .toList();

        return new GetDrinksResponse(responseDrinks);
    }

}
