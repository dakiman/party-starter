package com.example.partystarter.api;

import com.example.partystarter.BaseIntegrationTest;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.transaction.annotation.Transactional
class EventControllerPublicIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private String aliceJwt;

    @BeforeEach
    void setup() throws Exception {
        aliceJwt = registerAndGetJwt("alice", "alice@test.local", "alice-password-12");
    }

    @AfterEach
    void cleanup() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /events/public — returns public events, hides private")
    void publicFeed_filtersPrivate() throws Exception {
        createEvent(aliceJwt, false, "Open House", LocalDate.now().plusDays(7));
        createEvent(aliceJwt, true, "Secret Party", LocalDate.now().plusDays(7));

        mockMvc.perform(get("/events/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Open House"))
                .andExpect(jsonPath("$.content[0].isPrivate").value(false));
    }

    @Test
    @DisplayName("GET /events/public — works unauthenticated")
    void publicFeed_unauth() throws Exception {
        createEvent(aliceJwt, false, "Open House", LocalDate.now().plusDays(7));

        mockMvc.perform(get("/events/public"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /events/public?q=foo — filters by name LIKE")
    void publicFeed_search() throws Exception {
        createEvent(aliceJwt, false, "Sunset BBQ",   LocalDate.now().plusDays(1));
        createEvent(aliceJwt, false, "Brunch Club",  LocalDate.now().plusDays(2));
        createEvent(aliceJwt, false, "Sunday Roast", LocalDate.now().plusDays(3));

        mockMvc.perform(get("/events/public").param("q", "sun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[*].name", org.hamcrest.Matchers.containsInAnyOrder("Sunset BBQ", "Sunday Roast")));
    }

    @Test
    @DisplayName("GET /events/public — hides past events by default; includePast=true shows them")
    void publicFeed_pastFilter() throws Exception {
        createEvent(aliceJwt, false, "Past Party", LocalDate.now().minusDays(7));
        createEvent(aliceJwt, false, "Future Party", LocalDate.now().plusDays(7));

        mockMvc.perform(get("/events/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Future Party"));

        mockMvc.perform(get("/events/public").param("includePast", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("GET /events/public — clamps page size to 50")
    void publicFeed_clampsPageSize() throws Exception {
        mockMvc.perform(get("/events/public").param("size", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(50));
    }

    private String registerAndGetJwt(String username, String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username, "email", email, "password", password
        ));
        MvcResult res = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    private void createEvent(String jwt, boolean isPrivate, String name, LocalDate date) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", name,
                "date", date.toString(),
                "isPrivate", isPrivate,
                "artists", List.of(),
                "drinks", List.of(),
                "ingredients", List.of(),
                "food", List.of()
        ));
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
