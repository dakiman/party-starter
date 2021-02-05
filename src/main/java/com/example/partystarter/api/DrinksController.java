package com.example.partystarter.api;

import com.example.partystarter.model.Drink;
import com.example.partystarter.repo.DrinkRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/drinks")
@AllArgsConstructor
public class DrinksController {

    private final DrinkRepository drinkRepository;

    @GetMapping
    ResponseEntity<Iterable<Drink>> getDrinks() {
        return ResponseEntity.ok(drinkRepository.findAll());
    }

    @GetMapping(path = "/ingredients")
    ResponseEntity getDrink(@RequestParam List<String> ingredients) {
        return ResponseEntity.ok(ingredients);
    }
//
//    @GetMapping(path = "")
//    ResponseEntity getDrinksByIngredients(@RequestParam List<String> ingredients) {
//
//    }

}
