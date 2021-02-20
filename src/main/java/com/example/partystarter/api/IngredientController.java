package com.example.partystarter.api;

import com.example.partystarter.repo.IngredientRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/ingredients")
@AllArgsConstructor
public class IngredientController {

    private final IngredientRepository ingredientRepository;

    @GetMapping(path = "")
    ResponseEntity<List<String>> getAllIngredients() {
        return ResponseEntity.ok(ingredientRepository.getIngredientNames());
    }

}
