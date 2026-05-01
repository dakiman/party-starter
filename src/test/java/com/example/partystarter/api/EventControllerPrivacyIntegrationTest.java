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

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.transaction.annotation.Transactional
class EventControllerPrivacyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private String aliceJwt;
    private String bobJwt;
    private Integer publicEventId;
    private Integer privateEventId;

    @BeforeEach
    void setup() throws Exception {
        aliceJwt = registerAndGetJwt("alice", "alice@test.local", "alice-password-12");
        bobJwt = registerAndGetJwt("bob", "bob@test.local", "bob-password-12");

        publicEventId  = createEvent(aliceJwt, false, "Alice Public");
        privateEventId = createEvent(aliceJwt, true,  "Alice Private");
    }

    @AfterEach
    void cleanup() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /events/{id} — public event is readable unauthenticated")
    void publicEvent_unauth_ok() throws Exception {
        mockMvc.perform(get("/events/{id}", publicEventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(publicEventId));
    }

    @Test
    @DisplayName("GET /events/{id} — private event returns 401 unauthenticated")
    void privateEvent_unauth_unauthorized() throws Exception {
        mockMvc.perform(get("/events/{id}", privateEventId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /events/{id} — private event returns 200 for the creator")
    void privateEvent_creator_ok() throws Exception {
        mockMvc.perform(get("/events/{id}", privateEventId)
                        .header("Authorization", "Bearer " + aliceJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(privateEventId));
    }

    @Test
    @DisplayName("GET /events/{id} — private event returns 403 for an authed non-creator")
    void privateEvent_otherUser_forbidden() throws Exception {
        mockMvc.perform(get("/events/{id}", privateEventId)
                        .header("Authorization", "Bearer " + bobJwt))
                .andExpect(status().isForbidden());
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

    private Integer createEvent(String jwt, boolean isPrivate, String name) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", name,
                "date", "2027-01-01",
                "isPrivate", isPrivate,
                "artists", List.of(),
                "drinks", List.of(),
                "ingredients", List.of(),
                "food", List.of()
        ));
        MvcResult res = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asInt();
    }
}
