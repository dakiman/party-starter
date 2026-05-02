package com.example.partystarter.api;

import com.example.partystarter.BaseIntegrationTest;
import com.example.partystarter.repo.*;
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

class AttendeeRsvpIntegrationTest extends BaseIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private GuestUserRepository guestUserRepository;
    @Autowired private JoinRequestRepository joinRequestRepository;
    @Autowired private AttendeeRepository attendeeRepository;

    private String aliceJwt;
    private Integer publicEventId;
    private String guestToken;

    @BeforeEach
    void setup() throws Exception {
        aliceJwt = registerAndGetJwt("alice", "alice@test.local", "alice-password-12");
        publicEventId = createEvent(aliceJwt, false, "Public Party");
        String shareToken = issueShare(aliceJwt, publicEventId);
        // Guest claims attendance
        String body = objectMapper.writeValueAsString(Map.of("displayName", "Bob"));
        MvcResult r = mockMvc.perform(post("/share/{t}/request", shareToken)
            .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        guestToken = objectMapper.readTree(r.getResponse().getContentAsString()).get("guestToken").asText();
    }

    @AfterEach
    void cleanup() {
        attendeeRepository.deleteAll();
        joinRequestRepository.deleteAll();
        eventRepository.deleteAll();
        guestUserRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("PUT /events/{id}/attendees/me — guest changes status to MAYBE")
    void guestChangesStatus() throws Exception {
        mockMvc.perform(put("/events/{id}/attendees/me", publicEventId)
                .header("X-Guest-Token", guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"MAYBE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("MAYBE"));
    }

    @Test
    @DisplayName("PUT /events/{id}/attendees/me — caller without attendee row → 404")
    void noAttendeeRow_404() throws Exception {
        String otherJwt = registerAndGetJwt("eve", "eve@test.local", "eve-password-12");
        mockMvc.perform(put("/events/{id}/attendees/me", publicEventId)
                .header("Authorization", "Bearer " + otherJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"GOING\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /events/{id}/attendees/me — invalid status → 400")
    void invalidStatus_400() throws Exception {
        mockMvc.perform(put("/events/{id}/attendees/me", publicEventId)
                .header("X-Guest-Token", guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PARTYING\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /events/{id}/attendees/me — anonymous (no token at all) → 401")
    void anonymous_401() throws Exception {
        mockMvc.perform(put("/events/{id}/attendees/me", publicEventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"GOING\"}"))
            .andExpect(status().isUnauthorized());
    }

    // helpers
    private String registerAndGetJwt(String u, String e, String p) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", u, "email", e, "password", p));
        MvcResult r = mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }
    private Integer createEvent(String jwt, boolean isPrivate, String name) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "name", name, "date", "2027-01-01", "isPrivate", isPrivate,
            "artists", List.of(), "drinks", List.of(), "ingredients", List.of(), "food", List.of()));
        MvcResult r = mockMvc.perform(post("/events")
            .header("Authorization", "Bearer " + jwt)
            .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asInt();
    }
    private String issueShare(String jwt, Integer eventId) throws Exception {
        MvcResult r = mockMvc.perform(post("/events/{id}/share", eventId)
            .header("Authorization", "Bearer " + jwt)).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }
}
