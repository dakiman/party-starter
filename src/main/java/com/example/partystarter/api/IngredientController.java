package com.example.partystarter.api;

import com.example.partystarter.model.response.GetIngredientsResponse;
import com.example.partystarter.service.IngredientsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "/ingredients")
@AllArgsConstructor
public class IngredientController {

    private final IngredientsService ingredientsService;

    @GetMapping(path = "")
    public ResponseEntity<GetIngredientsResponse> getAllIngredients(
            @RequestParam(defaultValue = "true") boolean isAlcoholic,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(ingredientsService.getAllIngredients(isAlcoholic, name));
    }

}
