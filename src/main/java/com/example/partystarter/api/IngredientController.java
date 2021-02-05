package com.example.partystarter.api;

import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.service.DrinksService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/drinks")
@AllArgsConstructor
public class IngredientController {

    private final DrinksService drinksService;

    @GetMapping(path = "")
    ResponseEntity<GetDrinksResponse> getDrinksByIngredients(@RequestParam List<String> ingredients) {
        return ResponseEntity.ok(drinksService.getDrinksForIngredients(ingredients));
    }

}
