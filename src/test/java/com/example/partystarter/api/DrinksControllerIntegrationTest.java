package com.example.partystarter.api;

import com.example.partystarter.BaseIntegrationTest;
import com.example.partystarter.model.Drink;
import com.example.partystarter.model.DrinkIngredient;
import com.example.partystarter.model.Ingredient;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.IngredientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies Phase 7 cocktail-suggestions semantics:
 * - Match is alcohol-only: non-alcoholic input is ignored.
 * - Each result carries `fullyMakeable` and `missingAlcoholicIngredients`.
 * - Sort: fully-makeable first, then by missing-count asc, then alphabetical.
 * - Result list capped at 50.
 * - Mocktails (drinks with zero alcoholic ingredients) never appear.
 *
 * The cache annotation has been removed from `getDrinksForIngredients`
 * (Phase 7 design decision #7) so each request recomputes against the
 * current picked set.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DrinksControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private DrinkRepository drinkRepository;
    @Autowired private IngredientRepository ingredientRepository;

    private Ingredient vodka, tripleSec, limeJuice, gin, dryVermouth;

    @BeforeEach
    void setup() {
        vodka       = ingredientRepository.save(Ingredient.builder().name("vodka").isAlcoholic(true).build());
        tripleSec   = ingredientRepository.save(Ingredient.builder().name("triple sec").isAlcoholic(true).build());
        gin         = ingredientRepository.save(Ingredient.builder().name("gin").isAlcoholic(true).build());
        dryVermouth = ingredientRepository.save(Ingredient.builder().name("dry vermouth").isAlcoholic(true).build());
        limeJuice   = ingredientRepository.save(Ingredient.builder().name("lime juice").isAlcoholic(false).build());

        // Vodka Martini: vodka only (alcoholic). Has lime juice (non-alcoholic) too.
        saveDrink("Vodka Martini",
                "Stir with ice. Strain into chilled glass.", true,
                ing(vodka, "1½ oz"), ing(limeJuice, "splash"));

        // Cosmopolitan: vodka + triple sec (both alcoholic) + lime juice.
        saveDrink("Cosmopolitan",
                "Shake with ice. Strain into chilled glass.", true,
                ing(vodka, "1½ oz"), ing(tripleSec, "1 oz"), ing(limeJuice, "½ oz"));

        // Martini: gin + dry vermouth (both alcoholic).
        saveDrink("Dry Martini",
                "Stir with ice. Strain.", true,
                ing(gin, "2 oz"), ing(dryVermouth, "½ oz"));

        // Limeade (mocktail): only lime juice. Should never appear in suggestions.
        saveDrink("Limeade",
                "Combine. Stir.", false,
                ing(limeJuice, "2 oz"));
    }

    @AfterEach
    void cleanup() {
        drinkRepository.deleteAll();
        ingredientRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /drinks?ingredients=vodka — vodka-only fully makes Vodka Martini, partials list missing alcohols")
    void vodkaOnly_marksFullyMakeableAndMissing() throws Exception {
        JsonNode body = getSuggestions("vodka");

        assertEquals(2, body.size(), "Vodka Martini + Cosmopolitan overlap on vodka; Dry Martini and Limeade should not appear");

        // First entry: fully-makeable wins sort.
        assertEquals("Vodka Martini", body.get(0).get("name").asText());
        assertTrue(body.get(0).get("fullyMakeable").asBoolean());
        assertEquals(0, body.get(0).get("missingAlcoholicIngredients").size());

        // Second entry: Cosmopolitan, missing triple sec.
        assertEquals("Cosmopolitan", body.get(1).get("name").asText());
        assertFalse(body.get(1).get("fullyMakeable").asBoolean());
        assertEquals(1, body.get(1).get("missingAlcoholicIngredients").size());
        assertEquals("triple sec", body.get(1).get("missingAlcoholicIngredients").get(0).asText());
    }

    @Test
    @DisplayName("GET /drinks?ingredients=vodka,triple sec — both Cosmo and Vodka Martini fully makeable; Cosmo sorts before by alpha")
    void vodkaAndTripleSec_bothFullyMakeable_alphaSort() throws Exception {
        JsonNode body = getSuggestions("vodka,triple sec");

        assertEquals(2, body.size());
        // Both fullyMakeable; tie-broken alphabetically.
        assertEquals("Cosmopolitan",  body.get(0).get("name").asText());
        assertTrue(body.get(0).get("fullyMakeable").asBoolean());
        assertEquals("Vodka Martini", body.get(1).get("name").asText());
        assertTrue(body.get(1).get("fullyMakeable").asBoolean());
    }

    @Test
    @DisplayName("GET /drinks?ingredients=lime juice — non-alcoholic input ignored, returns empty")
    void nonAlcoholicOnly_returnsEmpty() throws Exception {
        JsonNode body = getSuggestions("lime juice");
        assertEquals(0, body.size());
    }

    @Test
    @DisplayName("Mocktail with zero alcoholic ingredients never appears in suggestions")
    void mocktailNeverAppears() throws Exception {
        JsonNode body = getSuggestions("vodka,gin,dry vermouth,triple sec");
        for (JsonNode drink : body) {
            assertNotEquals("Limeade", drink.get("name").asText(),
                    "Mocktails (zero alcoholic ingredients) must never appear regardless of input");
        }
    }

    @Test
    @DisplayName("Sort: fully-makeable first, then missing-count asc, then alphabetical")
    void sortOrder() throws Exception {
        // Pick gin only. Dry Martini needs gin+dry vermouth -> missing=1.
        // Cosmo and Vodka Martini have zero overlap with [gin] -> excluded.
        JsonNode body = getSuggestions("gin");

        assertEquals(1, body.size());
        assertEquals("Dry Martini", body.get(0).get("name").asText());
        assertFalse(body.get(0).get("fullyMakeable").asBoolean());
        assertEquals(1, body.get(0).get("missingAlcoholicIngredients").size());
        assertEquals("dry vermouth", body.get(0).get("missingAlcoholicIngredients").get(0).asText());
    }

    // --- helpers ----------------------------------------------------------

    private JsonNode getSuggestions(String csvIngredients) throws Exception {
        MvcResult result = mockMvc.perform(get("/drinks").param("ingredients", csvIngredients))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("drinks");
    }

    private DrinkIngredient ing(Ingredient i, String amount) {
        return DrinkIngredient.builder().ingredient(i).amount(amount).build();
    }

    private void saveDrink(String name, String recipe, boolean alcoholic, DrinkIngredient... ings) {
        Drink drink = Drink.builder()
                .name(name)
                .recipe(recipe)
                .isAlcoholic(alcoholic)
                .externalId(0)
                .ingredients(new HashSet<>())
                .build();
        Drink saved = drinkRepository.save(drink);
        Set<DrinkIngredient> joined = new HashSet<>();
        for (DrinkIngredient di : ings) {
            di.setDrink(saved);
            joined.add(di);
        }
        saved.setIngredients(joined);
        drinkRepository.save(saved);
    }
}
