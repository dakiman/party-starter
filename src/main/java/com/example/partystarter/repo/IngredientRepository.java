package com.example.partystarter.repo;

import com.example.partystarter.model.Ingredient;
import org.springframework.data.repository.CrudRepository;

public interface IngredientRepository extends CrudRepository<Ingredient, Integer> {
}
