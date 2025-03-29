package com.example.partystarter.service;

import com.example.partystarter.model.mapper.IngredientMapper;
import com.example.partystarter.model.response.GetIngredientsResponse;
import com.example.partystarter.model.response.GetIngredientsResponseIngredient;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class IngredientsService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    @Cacheable(cacheNames = "ingredients")
    public GetIngredientsResponse getAllIngredients(boolean isAlcoholic, String name) {
        List<GetIngredientsResponseIngredient> responseIngredients = ingredientRepository
                .findIngredients(isAlcoholic, name)
                .stream()
                .map(ingredientMapper::ingredientToGetIngredientsResponseIngredient)
                .toList();

        return new GetIngredientsResponse(responseIngredients);
    }

}
