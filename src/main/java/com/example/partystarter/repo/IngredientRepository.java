package com.example.partystarter.repo;

import com.example.partystarter.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    boolean existsByName(String name);

    Optional<Ingredient> getByName(String name);

    List<Ingredient> getIngredientByIsAlcoholic(boolean isAlcoholic);
}
