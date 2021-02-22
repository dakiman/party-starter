package com.example.partystarter.service;

import com.example.partystarter.repo.IngredientRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class IngredientsService {

    private final IngredientRepository ingredientRepository;

    public List<String> getAllIngredientNames() {
        return ingredientRepository.getIngredientNames();
    }

}
