package com.example.partystarter.model.mapper;

import com.example.partystarter.model.Ingredient;
import com.example.partystarter.model.cocktail.ExtendedIngredient;
import com.example.partystarter.model.response.GetIngredientsResponseIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class IngredientMapper {

    public abstract GetIngredientsResponseIngredient ingredientToGetIngredientsResponseIngredient(Ingredient ingredient);

    @Mapping(target = "drinks", ignore = true)
    @Mapping(target = "isAlcoholic", expression = "java(ingredient.getAlchohol() != null && ingredient.getAlchohol().equals(\"Yes\"))")
    public abstract com.example.partystarter.model.Ingredient extendedIngredientToIngredient(ExtendedIngredient ingredient);

}
