package com.example.partystarter.service;

import com.example.partystarter.model.mapper.DrinkMapper;
import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.model.response.GetDrinksResponseDrink;
import com.example.partystarter.repo.DrinkRepository;
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
    private final DrinkMapper drinkMapper;

    @Cacheable(cacheNames = "drinks")
    public GetDrinksResponse getDrinksForIngredients(List<String> ingredientNames) {
        List<GetDrinksResponseDrink> responseDrinks = Optional.ofNullable(ingredientNames)
                .map(drinkRepository::findDistinctByIngredientsIngredientNameIn)
                .orElseGet(drinkRepository::findAll)
                .stream()
                .map(drinkMapper::drinkToGetDrinksResponseDrink)
                .toList();
        return new GetDrinksResponse(responseDrinks);
    }

}
