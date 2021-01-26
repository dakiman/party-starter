package com.example.partystarter.repo;

import com.example.partystarter.model.Drink;
import org.springframework.data.repository.CrudRepository;

public interface DrinkRepository extends CrudRepository<Drink, Integer> {
}
