package com.example.partystarter.repo;

import com.example.partystarter.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    boolean existsByName(String name);

    Optional<Ingredient> getByName(String name);

    @Query("""
            SELECT i FROM Ingredient i
            WHERE i.isAlcoholic = :isAlcoholic
            AND (:name IS NULL OR LOWER(i.name) LIKE LOWER(concat('%', :name, '%')))
            ORDER BY i.name
            """)
    List<Ingredient> findIngredients(boolean isAlcoholic, String name);
}
