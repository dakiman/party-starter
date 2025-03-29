package com.example.partystarter.utils;

import com.example.partystarter.model.cocktail.ExtendedDrink;

import java.util.HashMap;
import java.util.Map;

import static com.example.partystarter.utils.ReflectionUtils.getFieldValue;

public class ConvertUtils {

    private ConvertUtils() {
    }

    public static Map<String, String> getIngredientsAndAmounts(ExtendedDrink drink) {
        Map<String, String> ingredientAmounts = new HashMap<>();

        for (int i = 1; i < 15; i++) {
            String name = getFieldValue(drink, "strIngredient" + i);
            String amount = getFieldValue(drink, "strMeasure" + i);

            if (name == null && amount == null)
                break;

            ingredientAmounts.put(name, amount);
        }

        return ingredientAmounts;
    }

}
