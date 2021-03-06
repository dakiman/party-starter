package com.example.partystarter.api;

import com.example.partystarter.model.Ingredient;
import com.example.partystarter.service.IngredientsService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class IngredientControllerTest {
//    private static final List<Ingredient> INGREDIENT_LIST = Collections.singletonList(Ingredient.builder().name("Test1").build());
//
//    private MockMvc mockMvc;
//
//    private ObjectMapper objectMapper;
//
//    @Mock
//    private IngredientsService ingredientsService;
//
//    @Before
//    public void setup() {
//        objectMapper = new ObjectMapper();
//        mockMvc = MockMvcBuilders
//                .standaloneSetup(new IngredientController(ingredientsService))
//                .build();
//    }
//
//    @Test
//    public void testGetAllIngredients() throws Exception {
//        when(ingredientsService.getAllIngredients()).thenReturn(INGREDIENT_LIST);
//
//        List<String> response = objectMapper.readValue(
//            mockMvc.perform(get("/ingredients"))
//            .andExpect(status().isOk())
//            .andReturn()
//            .getResponse().getContentAsString(), List.class
//        );
//
//        verify(ingredientsService, times(1)).getAllIngredients();
//        verifyNoMoreInteractions(ingredientsService);
//
//        assertEquals(response, INGREDIENT_LIST);
//    }

}
