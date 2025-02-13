package com.example.partystarter.utils;

import com.example.partystarter.model.*;
import com.example.partystarter.model.cocktail.ExtendedDrink;
import com.example.partystarter.model.cocktail.ExtendedIngredient;
import com.example.partystarter.model.response.*;

import java.util.*;

import static com.example.partystarter.utils.ReflectionUtils.getFieldValue;

public class ConvertUtils {

    private ConvertUtils() {}

    public static Drink mapDrink(ExtendedDrink drink) {
        return Drink.builder()
                .name(drink.getStrDrink())
                .recipe(drink.getStrInstructions())
                .externalId(Integer.parseInt(drink.getIdDrink()))
                .isAlcoholic(drink.getStrAlcoholic().equals("Alcoholic"))
                .thumbnail(drink.getStrDrinkThumb())
                .build();
    }

    public static Ingredient mapIngredient(ExtendedIngredient ingredient) {
        return Ingredient.builder()
                .name(ingredient.getName())
                .abv(ingredient.getAbv())
                .description(ingredient.getDescription())
                .isAlcoholic(ingredient.getAlchohol() != null && ingredient.getAlchohol().equals("Yes"))
                .build();
    }


    public static List<GetDrinksResponseIngredient> mapIngredients(Set<DrinkIngredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> GetDrinksResponseIngredient.builder()
                        .name(ingredient.getIngredient().getName())
                        .isAlcoholic(ingredient.getIngredient().getIsAlcoholic())
                        .abv(ingredient.getIngredient().getAbv())
                        .amount(ingredient.getAmount())
                        .build())
                .toList();
    }

    public static GetDrinksResponseDrink mapDrinksToResponse(Drink drink) {
        return GetDrinksResponseDrink.builder()
                .id(drink.getId())
                .isAlcoholic(drink.getIsAlcoholic())
                .thumbnail(drink.getThumbnail())
                .name(drink.getName())
                .recipe(drink.getRecipe())
                .ingredients(mapIngredients(drink.getIngredients()))
                .build();
    }

    public static GetIngredientsResponseIngredient mapIngredientsToResponse(Ingredient ingredient) {
        return GetIngredientsResponseIngredient.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .abv(ingredient.getAbv())
                .isAlcoholic(ingredient.getIsAlcoholic())
                .description(ingredient.getDescription())
                .build();
    }

    public static PartyResponse mapPartyToResponse(Party party) {
        return PartyResponse.builder()
                .id(party.getId())
                .name(party.getName())
                .date(party.getDate())
                .time(party.getTime())
                .location(party.getLocation() != null ? PartyResponse.LocationResponse.builder()
                        .latitude(party.getLocation().getLatitude())
                        .longitude(party.getLocation().getLongitude())
                        .description(party.getLocation().getDescription())
                        .build() : null)
                .artists(party.getArtists().stream()
                        .map(artist -> PartyResponse.ArtistResponse.builder()
                                .spotifyId(artist.getSpotifyId())
                                .name(artist.getName())
                                .images(artist.getImages())
                                .genres(new ArrayList<>(artist.getGenres()))
                                .spotifyUrl(artist.getSpotifyUrl())
                                .build())
                        .toList())
                .drinks(party.getDrinks().stream()
                        .map(ConvertUtils::mapDrinksToResponse)
                        .toList())
                .food(party.getFoodItems())
                .createdAt(party.getCreatedAt())
                .updatedAt(party.getUpdatedAt())
                .build();
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
