package com.example.partystarter.api;

import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.service.DrinksService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/drinks")
@AllArgsConstructor
public class DrinkController {

    private final DrinksService drinksService;

    @GetMapping
    public ResponseEntity<GetDrinksResponse> getDrinksByIngredients(@RequestParam(required = false) List<String> ingredients) {
        return ResponseEntity.ok(drinksService.getDrinksForIngredients(ingredients));
    }

}
