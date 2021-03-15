package com.example.partystarter.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GetIngredientsResponse {
    private List<GetIngredientsResponseIngredient> ingredients;
}
