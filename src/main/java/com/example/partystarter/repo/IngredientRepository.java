package com.example.partystarter.repo;

import com.example.partystarter.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;


public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    boolean existsByName(String name);
}
