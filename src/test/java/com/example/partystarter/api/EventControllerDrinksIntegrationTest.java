package com.example.partystarter.api;

import com.example.partystarter.BaseIntegrationTest;
import com.example.partystarter.model.Drink;
import com.example.partystarter.model.DrinkIngredient;
import com.example.partystarter.model.Ingredient;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.repo.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that POST /events with drinks=[id1, id2] round-trips through
 * EventResponse.drinks with recipe and ingredient amounts populated. (A1)
 */
class EventControllerDrinksIntegrationTest extends BaseIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private DrinkRepository drinkRepository;
    @Autowired private IngredientRepository ingredientRepository;

    private String aliceJwt;
    private Integer cosmoId;
    private Integer martiniId;

    @BeforeEach
    void setup() throws Exception {
        aliceJwt = registerAndGetJwt("alice", "alice@test.local", "alice-password-12");

        Ingredient vodka     = ingredientRepository.save(Ingredient.builder().name("vodka").isAlcoholic(true).build());
        Ingredient tripleSec = ingredientRepository.save(Ingredient.builder().name("triple sec").isAlcoholic(true).build());
        Ingredient gin       = ingredientRepository.save(Ingredient.builder().name("gin").isAlcoholic(true).build());

        cosmoId   = saveDrink("Cosmopolitan", "Shake with ice. Strain.",
                              vodka, "1½ oz", tripleSec, "1 oz");
        martiniId = saveDrink("Dry Martini",  "Stir with ice.",
                              gin,   "2 oz");
    }

    @AfterEach
    void cleanup() {
        eventRepository.deleteAll();
        drinkRepository.deleteAll();
        ingredientRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /events with drinks=[cosmo, martini] then GET — response.drinks contains both with recipe + ingredient amounts")
    void createWithDrinks_roundTrips() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Saturday party",
                "date", LocalDate.now().plusDays(7).toString(),
                "isPrivate", true,
                "drinks", List.of(cosmoId, martiniId),
                "ingredients", List.of(),
                "food", List.of(),
                "artists", List.of()
        );

        MvcResult res = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + aliceJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        Integer eventId = objectMapper.readTree(res.getResponse().getContentAsString())
                .get("id").asInt();

        MvcResult get = mockMvc.perform(get("/events/{id}", eventId)
                        .header("Authorization", "Bearer " + aliceJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.drinks").isArray())
                .andReturn();

        JsonNode drinks = objectMapper.readTree(get.getResponse().getContentAsString()).get("drinks");
        assertEquals(2, drinks.size());

        for (JsonNode drink : drinks) {
            String name = drink.get("name").asText();
            assertNotNull(drink.get("recipe").asText(), "recipe must be populated");
            assertTrue(drink.get("ingredients").isArray());
            assertTrue(drink.get("ingredients").size() > 0,
                    name + " should have at least one ingredient with amount");
            assertNotNull(drink.get("ingredients").get(0).get("amount").asText());
        }
    }

    private Integer saveDrink(String name, String recipe, Object... ingredientAmountPairs) {
        Drink drink = Drink.builder()
                .name(name)
                .recipe(recipe)
                .isAlcoholic(true)
                .externalId(0)
                .ingredients(new HashSet<>())
                .build();
        Drink saved = drinkRepository.save(drink);
        HashSet<DrinkIngredient> joins = new HashSet<>();
        for (int i = 0; i < ingredientAmountPairs.length; i += 2) {
            Ingredient ing = (Ingredient) ingredientAmountPairs[i];
            String amt = (String) ingredientAmountPairs[i + 1];
            DrinkIngredient di = DrinkIngredient.builder()
                    .drink(saved).ingredient(ing).amount(amt).build();
            joins.add(di);
        }
        saved.setIngredients(joins);
        return drinkRepository.save(saved).getId();
    }

    private String registerAndGetJwt(String username, String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "email", email,
                "password", password
        ));
        MvcResult res = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }
}
