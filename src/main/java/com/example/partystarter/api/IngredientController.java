package com.example.partystarter.api;

import com.example.partystarter.model.Ingredient;
import com.example.partystarter.model.response.GetIngredientsResponse;
import com.example.partystarter.model.response.GetIngredientsResponseIngredient;
import com.example.partystarter.service.IngredientsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/ingredients")
@AllArgsConstructor
public class IngredientController {

    private final IngredientsService ingredientsService;

    @GetMapping(path = "")
    ResponseEntity<GetIngredientsResponse> getAllIngredients(@RequestParam(required = false) Boolean isAlcoholic) {
        return ResponseEntity.ok(ingredientsService.getAllIngredients(isAlcoholic));
    }

}
