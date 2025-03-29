package com.example.partystarter.service;

import com.example.partystarter.model.Drink;
import com.example.partystarter.model.DrinkIngredient;
import com.example.partystarter.model.Ingredient;
import com.example.partystarter.model.mapper.DrinkMapper;
import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.model.response.GetDrinksResponseDrink;
import com.example.partystarter.repo.DrinkRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DrinksServiceTest {

    private DrinksService drinksService;

    @Mock
    private DrinkRepository drinkRepository;

    @Mock
    private DrinkMapper drinkMapper;

    @Before
    public void setup() {
        drinksService = new DrinksService(drinkRepository, drinkMapper);
    }

    @Test
    public void testGetAllDrinks() {
        Drink mockDrink = getDrink();

        when(drinkRepository.findAll()).thenReturn(Collections.singletonList(mockDrink));
        GetDrinksResponseDrink responseDrink = getGetDrinksResponseDrink(mockDrink);
        when(drinkMapper.drinkToGetDrinksResponseDrink(mockDrink)).thenReturn(responseDrink);

        GetDrinksResponse getDrinksResponse = drinksService.getDrinksForIngredients(null);

        verify(drinkRepository, times(1)).findAll();
        verifyNoMoreInteractions(drinkRepository);

        assertEquals(getDrinksResponse.getDrinks().get(0), responseDrink);
    }

    private static GetDrinksResponseDrink getGetDrinksResponseDrink(Drink mockDrink) {
        return GetDrinksResponseDrink.builder()
                .id(mockDrink.getId())
                .thumbnail(mockDrink.getThumbnail())
                .name(mockDrink.getName())
                .isAlcoholic(Boolean.TRUE)
                .recipe(mockDrink.getRecipe())
                .build();
    }

    public Drink getDrink() {
        Ingredient ingredient = Ingredient.builder()
                .name("Test name")
                .abv("40")
                .description("Test description")
                .isAlcoholic(false)
                .id(123)
                .build();

        Drink drink = Drink.builder()
                .externalId(123)
                .recipe("Test recipe")
                .name("Test name")
                .isAlcoholic(true)
                .id(123)
                .build();

        Set<DrinkIngredient> ingredientSet = new HashSet<>();
        ingredientSet.add(DrinkIngredient.builder().drink(drink).ingredient(ingredient).amount("123").id(123L).build());

        drink.setIngredients(ingredientSet);
        ingredient.setDrinks(ingredientSet);

        return drink;
    }
}
