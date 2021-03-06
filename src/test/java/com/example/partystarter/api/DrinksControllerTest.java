package com.example.partystarter.api;

import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.model.response.GetDrinksResponseDrink;
import com.example.partystarter.model.response.GetDrinksResponseIngredient;
import com.example.partystarter.service.DrinksService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class DrinksControllerTest {
    private static final String INGREDIENT_NAME = "Test";

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private DrinksService drinksService;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DrinkController(drinksService))
                .build();
    }

    @Test
    public void testGetDrinks() throws Exception {
        GetDrinksResponse getDrinksResponse = getDrinksResponse();

        when(drinksService.getAllDrinks()).thenReturn(getDrinksResponse);

        GetDrinksResponse response = objectMapper.readValue(
                mockMvc.perform(get("/drinks"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse().getContentAsString(), GetDrinksResponse.class);

        verify(drinksService, times(1)).getAllDrinks();
        verifyNoMoreInteractions(drinksService);

        assertEquals(response.getDrinks().get(0), getDrinksResponse.getDrinks().get(0));
    }

    @Test
    public void testGetDrinksByIngredient() throws Exception {
        GetDrinksResponse getDrinksResponse = getDrinksResponse();
        List<String> ingredientArguments = Collections.singletonList(INGREDIENT_NAME);

        when(drinksService.getDrinksForIngredients(eq(ingredientArguments))).thenReturn(getDrinksResponse);

        GetDrinksResponse response = objectMapper.readValue(
                mockMvc.perform(get("/drinks?ingredients=" + INGREDIENT_NAME))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse().getContentAsString(), GetDrinksResponse.class);

        verify(drinksService, times(1)).getDrinksForIngredients(ingredientArguments);
        verifyNoMoreInteractions(drinksService);

        assertEquals(response.getDrinks().get(0), getDrinksResponse.getDrinks().get(0));
    }

    private GetDrinksResponse getDrinksResponse() {
        List<GetDrinksResponseIngredient> ingredients = Collections.singletonList(GetDrinksResponseIngredient.builder()
                .amount("1/2")
                .isAlcoholic(true)
                .abv("40")
                .name("Test")
                .build());

        List<GetDrinksResponseDrink> drinks = Collections.singletonList(GetDrinksResponseDrink.builder()
                .name("Drink")
                .isAlcoholic(true)
                .recipe(INGREDIENT_NAME)
                .ingredients(ingredients)
                .build());

        return new GetDrinksResponse(drinks);
    }

}
