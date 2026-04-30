package com.example.partystarter.api;

import com.example.partystarter.BaseIntegrationTest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PUT /events/{id} and DELETE /events/{id}.
 *
 * Strategy:
 * 1. @BeforeEach: register "alice", create one event, capture jwt + eventId.
 * 2. Each test exercises one status-code scenario.
 * 3. @AfterEach: wipe events + users.
 */
@org.springframework.transaction.annotation.Transactional
class EventControllerEditDeleteIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private String aliceJwt;
    private Integer eventId;

    private static final Map<String, Object> VALID_UPDATE_BODY = Map.of(
            "name", "Updated Event Name",
            "date", "2027-06-01",
            "isPrivate", false,
            "artists", List.of(),
            "drinks", List.of(),
            "ingredients", List.of(),
            "food", List.of("Tacos")
    );

    @BeforeEach
    void setup() throws Exception {
        // Register Alice
        String aliceReg = objectMapper.writeValueAsString(Map.of(
                "username", "alice",
                "email", "alice@test.local",
                "password", "alice-password-12"
        ));
        MvcResult regRes = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aliceReg))
                .andExpect(status().isOk())
                .andReturn();
        aliceJwt = objectMapper.readTree(regRes.getResponse().getContentAsString())
                .get("token").asText();

        // Create one event owned by Alice
        String createBody = objectMapper.writeValueAsString(Map.of(
                "name", "Alice's Party",
                "date", "2027-01-01",
                "isPrivate", true,
                "artists", List.of(),
                "drinks", List.of(),
                "ingredients", List.of(),
                "food", List.of("Pizza")
        ));
        MvcResult createRes = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + aliceJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();
        eventId = objectMapper.readTree(createRes.getResponse().getContentAsString())
                .get("id").asInt();
    }

    @AfterEach
    void cleanup() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ─── PUT /events/{id} ────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /events/{id} — creator can update the event")
    void updateEvent_success() throws Exception {
        String body = objectMapper.writeValueAsString(VALID_UPDATE_BODY);

        mockMvc.perform(put("/events/{id}", eventId)
                        .header("Authorization", "Bearer " + aliceJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.name").value("Updated Event Name"))
                .andExpect(jsonPath("$.date").value("2027-06-01"))
                .andExpect(jsonPath("$.isPrivate").value(false))
                .andExpect(jsonPath("$.food.length()").value(1))
                .andExpect(jsonPath("$.food[0]").value("Tacos"))
                .andExpect(jsonPath("$.creatorUsername").value("alice"));

        // Verify DB state
        var stored = eventRepository.findById(eventId).orElseThrow();
        assertEquals("Updated Event Name", stored.getName());
        assertEquals(1, stored.getFoodItems().size());
        assertEquals("Tacos", stored.getFoodItems().get(0));
        assertFalse(stored.getIsPrivate());
    }

    @Test
    @DisplayName("PUT /events/{id} — returns 404 when event does not exist")
    void updateEvent_notFound() throws Exception {
        String body = objectMapper.writeValueAsString(VALID_UPDATE_BODY);

        mockMvc.perform(put("/events/{id}", 999_999)
                        .header("Authorization", "Bearer " + aliceJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /events/{id} — returns 403 when caller is not the creator")
    void updateEvent_nonCreator_forbidden() throws Exception {
        // Register Bob
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
        String bobJwt = objectMapper.readTree(bobRes.getResponse().getContentAsString())
                .get("token").asText();

        String body = objectMapper.writeValueAsString(VALID_UPDATE_BODY);

        // Bob attempts to update Alice's event
        mockMvc.perform(put("/events/{id}", eventId)
                        .header("Authorization", "Bearer " + bobJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        // Alice's event must be unchanged
        var stored = eventRepository.findById(eventId).orElseThrow();
        assertEquals("Alice's Party", stored.getName());
    }

    @Test
    @DisplayName("PUT /events/{id} — returns 401 without a JWT")
    void updateEvent_noAuth() throws Exception {
        String body = objectMapper.writeValueAsString(VALID_UPDATE_BODY);

        mockMvc.perform(put("/events/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ─── DELETE /events/{id} ─────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /events/{id} — creator can delete the event")
    void deleteEvent_success() throws Exception {
        mockMvc.perform(delete("/events/{id}", eventId)
                        .header("Authorization", "Bearer " + aliceJwt))
                .andExpect(status().isNoContent());

        // Event must be gone from DB
        assertFalse(eventRepository.findById(eventId).isPresent());
    }

    @Test
    @DisplayName("DELETE /events/{id} — returns 404 when event does not exist")
    void deleteEvent_notFound() throws Exception {
        mockMvc.perform(delete("/events/{id}", 999_999)
                        .header("Authorization", "Bearer " + aliceJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /events/{id} — returns 403 when caller is not the creator")
    void deleteEvent_nonCreator_forbidden() throws Exception {
        // Register Bob
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
        String bobJwt = objectMapper.readTree(bobRes.getResponse().getContentAsString())
                .get("token").asText();

        // Bob attempts to delete Alice's event
        mockMvc.perform(delete("/events/{id}", eventId)
                        .header("Authorization", "Bearer " + bobJwt))
                .andExpect(status().isForbidden());

        // Alice's event must still exist
        assertTrue(eventRepository.findById(eventId).isPresent());
    }

    @Test
    @DisplayName("DELETE /events/{id} — returns 401 without a JWT")
    void deleteEvent_noAuth() throws Exception {
        mockMvc.perform(delete("/events/{id}", eventId))
                .andExpect(status().isUnauthorized());
    }
}
