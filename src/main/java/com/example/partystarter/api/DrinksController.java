package com.example.partystarter.api;

import com.example.partystarter.model.Drink;
import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.service.DrinksService;
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
    private final DrinksService drinksService;

//    @GetMapping
//    ResponseEntity<Iterable<Drink>> getDrinks() {
//        return ResponseEntity.ok(drinkRepository.findAll());
//    }

//    @GetMapping(path = "/ingredients")
//    ResponseEntity getDrink(@RequestParam List<String> ingredients) {
//        return ResponseEntity.ok(ingredients);
//    }

    @GetMapping(path = "")
    ResponseEntity<GetDrinksResponse> getDrinksByIngredients(@RequestParam List<String> ingredients) {
        return ResponseEntity.ok(drinksService.getDrinksForIngredients(ingredients));
    }

}
