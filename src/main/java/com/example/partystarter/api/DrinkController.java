package com.example.partystarter.api;

import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.service.DrinksService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/drinks")
@AllArgsConstructor
public class DrinkController {

    private final DrinksService drinksService;

    @GetMapping(params = {"ingredients"}) // params required for overloading endpoint
    public ResponseEntity<GetDrinksResponse> getDrinksByIngredients(@RequestParam List<String> ingredients) {
        return ResponseEntity.ok(drinksService.getDrinksForIngredients(ingredients));
    }

    @GetMapping
    public ResponseEntity<GetDrinksResponse> getAllDrinks() {
        return ResponseEntity.ok(drinksService.getAllDrinks());
    }

}
