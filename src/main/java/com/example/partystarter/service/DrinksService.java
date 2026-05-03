package com.example.partystarter.service;

import com.example.partystarter.model.Drink;
import com.example.partystarter.model.Ingredient;
import com.example.partystarter.model.response.GetDrinksResponse;
import com.example.partystarter.model.response.GetDrinksResponseDrink;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class DrinksService {

    /** Bound on the number of suggestions returned per request. Sort places the
     * most-makeable drinks first, so truncation never hides a fully-makeable
     * row in realistic input. See Phase 7 design decision #8. */
    private static final int MAX_SUGGESTIONS = 50;

    private final DrinkRepository drinkRepository;

    /**
     * Returns cocktails matched against the picked ingredient set, with
     * completeness scoring computed on alcoholic ingredients only.
     *
     * <p>Matching rules (Phase 7):
     * <ul>
     *   <li>Non-alcoholic names in the input are ignored. Mixers / citrus /
     *       sugar are treated as commodity items the host will have on hand.</li>
     *   <li>A drink is a candidate iff its alcoholic-ingredient set has
     *       non-empty intersection with the picked alcohols. (Zero-overlap
     *       drinks are pure noise.)</li>
     *   <li>{@code fullyMakeable} is true iff every alcoholic ingredient the
     *       drink requires is in the picked set.</li>
     *   <li>{@code missingAlcoholicIngredients} lists alcoholic ingredients
     *       the drink needs but the picked set lacks.</li>
     *   <li>Sort: fully-makeable first; then by missing-count ascending; then
     *       by name alphabetical.</li>
     *   <li>Capped at {@value #MAX_SUGGESTIONS}.</li>
     * </ul>
     *
     * <p>Implementation note: in-memory pass over {@code drinkRepository.findAll()}.
     * The seed is ~600 drinks; a SQL pre-filter is unnecessary at this scale
     * and would brittle the alcohol-only logic (which is naturally expressed
     * over loaded entities). Revisit if perf becomes a concern.
     *
     * <p>The {@code @Cacheable} annotation that was on this method has been
     * removed: results are now a function of the picked set rather than a
     * straight repository read, and the cache key (a {@link List}) was
     * order-sensitive. Recomputing per request is sub-millisecond at seed scale.
     */
    @Transactional(readOnly = true)
    public GetDrinksResponse getDrinksForIngredients(List<String> ingredientNames) {
        if (ingredientNames == null || ingredientNames.isEmpty()) {
            return new GetDrinksResponse(List.of());
        }

        Set<String> pickedAlcohols = ingredientNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (pickedAlcohols.isEmpty()) {
            return new GetDrinksResponse(List.of());
        }

        List<GetDrinksResponseDrink> ranked = drinkRepository.findAll().stream()
                .map(drink -> scoreDrink(drink, pickedAlcohols))
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing((GetDrinksResponseDrink d) -> !d.isFullyMakeable())
                        .thenComparingInt(d -> d.getMissingAlcoholicIngredients().size())
                        .thenComparing(GetDrinksResponseDrink::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_SUGGESTIONS)
                .toList();

        return new GetDrinksResponse(ranked);
    }

    @Transactional(readOnly = true)
    public GetDrinksResponse getAllDrinks() {
        List<GetDrinksResponseDrink> responseDrinks = drinkRepository.findAll().stream()
                .map(ConvertUtils::mapDrinksToResponse)
                .toList();
        return new GetDrinksResponse(responseDrinks);
    }

    /**
     * Builds a response DTO with completeness scoring, OR returns null when
     * the drink fails the candidate test (no overlap with picked alcohols, or
     * has zero alcoholic ingredients — i.e. is a mocktail).
     */
    private GetDrinksResponseDrink scoreDrink(Drink drink, Set<String> pickedAlcoholsLower) {
        Set<String> requiredAlcohols = drink.getIngredients().stream()
                .map(di -> di.getIngredient())
                .filter(i -> Boolean.TRUE.equals(i.getIsAlcoholic()))
                .map(Ingredient::getName)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (requiredAlcohols.isEmpty()) {
            return null; // Mocktail — never a suggestion.
        }

        Set<String> intersection = new LinkedHashSet<>(requiredAlcohols);
        intersection.retainAll(pickedAlcoholsLower);
        if (intersection.isEmpty()) {
            return null; // No overlap → not a candidate.
        }

        List<String> missing = requiredAlcohols.stream()
                .filter(name -> !pickedAlcoholsLower.contains(name))
                .toList();

        GetDrinksResponseDrink dto = ConvertUtils.mapDrinksToResponse(drink);
        dto.setMissingAlcoholicIngredients(missing);
        dto.setFullyMakeable(missing.isEmpty());
        return dto;
    }
}
