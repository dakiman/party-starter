package com.example.partystarter.repo;

import com.example.partystarter.model.Drink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrinkRepository extends JpaRepository<Drink, Integer> {
    boolean existsByName(String name);
    List<Drink> findAllByIngredientsIngredientNameIn(List<String> names);
}
