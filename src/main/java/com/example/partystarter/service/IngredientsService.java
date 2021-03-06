package com.example.partystarter.service;

import com.example.partystarter.model.Ingredient;
import com.example.partystarter.model.response.GetIngredientsResponse;
import com.example.partystarter.model.response.GetIngredientsResponseIngredient;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.partystarter.utils.ConvertUtils.mapIngredientsToResponse;

@Slf4j
@Service
@AllArgsConstructor
public class IngredientsService {

    private final IngredientRepository ingredientRepository;

    public GetIngredientsResponse getAllIngredients(@Nullable Boolean isAlcoholic) {
        List<Ingredient> ingredients;

        if (isAlcoholic != null) {
            ingredients = ingredientRepository.getIngredientByIsAlcoholic(isAlcoholic);
        } else {
            ingredients = ingredientRepository.findAll();
        }

        List<GetIngredientsResponseIngredient> responseIngredients = ingredients
                .stream()
                .map(ConvertUtils::mapIngredientsToResponse)
                .collect(Collectors.toList());

        return new GetIngredientsResponse(responseIngredients);
    }

}
