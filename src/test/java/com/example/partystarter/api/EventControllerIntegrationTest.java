package com.example.partystarter.api;

import com.example.partystarter.BaseIntegrationTest;
import com.example.partystarter.model.User;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.repo.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end coverage of the existing /events flows. Anchors every later
 * feature that touches EventController/EventService.
 *
 * Strategy:
 * 1. Register a test user via /auth/register to obtain a real JWT.
 * 2. Issue /events requests with that bearer token.
 * 3. Assert response shape AND DB state via the JPA repositories.
 * 4. Wipe `event` and `user` tables in @AfterEach so tests stay isolated.
 *
 * Note: `@Transactional` keeps the Hibernate session open across the whole test
 * method so post-call `eventRepository.findById(...).getFoodItems()` lazy-loads
 * cleanly. Spring rolls back the transaction after each test; @AfterEach cleanup
 * sees an empty DB and is a no-op.
 */
@org.springframework.transaction.annotation.Transactional
class EventControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private String jwt;
    private User testUser;

    @BeforeEach
    void registerTestUser() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", "alice",
                "email", "alice@test.local",
                "password", "alice-password-12"
        ));

        MvcResult res = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        JsonNode root = objectMapper.readTree(res.getResponse().getContentAsString());
        jwt = root.get("token").asText();
        testUser = userRepository.getByUsername("alice").orElseThrow();
    }

    @AfterEach
    void cleanup() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /events creates an event for the authenticated user")
    void createEvent() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Alice's birthday",
                "date", "2026-12-31",
                "time", "20:00",
                "location", Map.of(
                        "lat", 41.9981,
                        "lng", 21.4254,
                        "locationDescription", "Skopje city center"
                ),
                "artists", List.of(),
                "drinks", List.of(),
                "ingredients", List.of(),
                "food", List.of("Pizza", "Cake"),
                "isPrivate", true
        ));

        MvcResult res = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(greaterThan(0)))
                .andExpect(jsonPath("$.name").value("Alice's birthday"))
                .andExpect(jsonPath("$.date").value("2026-12-31"))
                .andExpect(jsonPath("$.time").value("20:00"))
                .andExpect(jsonPath("$.isPrivate").value(true))
                .andExpect(jsonPath("$.location.latitude").value(41.9981))
                .andExpect(jsonPath("$.location.longitude").value(21.4254))
                .andExpect(jsonPath("$.location.description").value("Skopje city center"))
                .andExpect(jsonPath("$.food.length()").value(2))
                .andReturn();

        JsonNode root = objectMapper.readTree(res.getResponse().getContentAsString());
        Integer eventId = root.get("id").asInt();

        // Verify DB state via repository
        var stored = eventRepository.findById(eventId).orElseThrow();
        assertEquals("Alice's birthday", stored.getName());
        assertEquals(testUser.getId(), stored.getCreator().getId());
        assertEquals(2, stored.getFoodItems().size());
        assertTrue(stored.getIsPrivate());
    }

    @Test
    @DisplayName("POST /events returns 401 without a JWT")
    void createEventRequiresAuth() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "no auth",
                "date", "2026-12-31",
                "isPrivate", false
        ));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /events/{id} returns the created event")
    void getEvent() throws Exception {
        // Arrange — create an event first
        String createBody = objectMapper.writeValueAsString(Map.of(
                "name", "Dinner",
                "date", "2026-06-15",
                "isPrivate", false
        ));
        MvcResult createRes = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();
        Integer id = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asInt();

        // Act + Assert
        mockMvc.perform(get("/events/{id}", id)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Dinner"))
                .andExpect(jsonPath("$.isPrivate").value(false));
    }

    @Test
    @DisplayName("GET /events/{id} returns 404 for a missing event")
    void getEventMissing() throws Exception {
        mockMvc.perform(get("/events/{id}", 999_999)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /events?createdBy=ME returns this user's events, newest first")
    void listMyEvents() throws Exception {
        // Arrange — create three events
        for (String name : List.of("first", "second", "third")) {
            String body = objectMapper.writeValueAsString(Map.of(
                    "name", name,
                    "date", "2026-12-31",
                    "isPrivate", false
            ));
            mockMvc.perform(post("/events")
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }

        // Act + Assert
        mockMvc.perform(get("/events").param("createdBy", "ME")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                // findByCreatorOrderByCreatedAtDesc → newest first
                .andExpect(jsonPath("$[0].name").value("third"))
                .andExpect(jsonPath("$[1].name").value("second"))
                .andExpect(jsonPath("$[2].name").value("first"));
    }

    @Test
    @DisplayName("GET /events?createdBy=ME does not return another user's events")
    void listMyEventsIsolatesPerUser() throws Exception {
        // Alice creates an event
        String aliceBody = objectMapper.writeValueAsString(Map.of(
                "name", "alice's",
                "date", "2026-12-31",
                "isPrivate", true
        ));
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aliceBody))
                .andExpect(status().isOk());

        // Bob registers and creates his own event
        String bobReg = objectMapper.writeValueAsString(Map.of(
                "username", "bob",
                "email", "bob@test.local",
                "password", "bob-password-12"
        ));
        MvcResult bobRes = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bobReg))
                .andExpect(status().isOk())
                .andReturn();
        String bobJwt = objectMapper.readTree(bobRes.getResponse().getContentAsString()).get("token").asText();

        String bobBody = objectMapper.writeValueAsString(Map.of(
                "name", "bob's",
                "date", "2026-12-31",
                "isPrivate", false
        ));
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + bobJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bobBody))
                .andExpect(status().isOk());

        // Bob asks for HIS events — should see only his
        mockMvc.perform(get("/events").param("createdBy", "ME")
                        .header("Authorization", "Bearer " + bobJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("bob's"));

        // Alice asks for HER events — should see only hers
        mockMvc.perform(get("/events").param("createdBy", "ME")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("alice's"));
    }

    @Test
    @DisplayName("GET /events without auth returns 401")
    void listEventsRequiresAuth() throws Exception {
        mockMvc.perform(get("/events").param("createdBy", "ME"))
                .andExpect(status().isUnauthorized());
    }
}
