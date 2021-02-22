package com.example.partystarter.service;

import com.example.partystarter.model.Drink;
import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.model.response.GetDrinksResponseDrink;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class DrinksService {

    private final DrinkRepository drinkRepository;

    public GetDrinksResponse getDrinksForIngredients(List<String> ingredientNames) {
        List<Drink> drinks = drinkRepository.findDistinctByIngredientsIngredientNameIn(ingredientNames);

        List<GetDrinksResponseDrink> responseDrinks = drinks
                .stream()
                .map(ConvertUtils::mapDrinksToResponse)
                .collect(Collectors.toList());

        return new GetDrinksResponse(responseDrinks);
    }

    public GetDrinksResponse getAllDrinks() {
        List<GetDrinksResponseDrink> responseDrinks = drinkRepository
                .findAll()
                .stream()
                .map(ConvertUtils::mapDrinksToResponse)
                .collect(Collectors.toList());

        return new GetDrinksResponse(responseDrinks);
    }

}
