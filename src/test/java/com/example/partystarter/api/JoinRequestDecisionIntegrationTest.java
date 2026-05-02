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

class JoinRequestDecisionIntegrationTest extends BaseIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private GuestUserRepository guestUserRepository;
    @Autowired private JoinRequestRepository joinRequestRepository;
    @Autowired private AttendeeRepository attendeeRepository;

    private String aliceJwt;
    private String bobJwt;
    private Integer privateEventId;
    private String shareToken;
    private Long pendingRequestId;
    private String guestToken;

    @BeforeEach
    void setup() throws Exception {
        aliceJwt = registerAndGetJwt("alice", "alice@test.local", "alice-password-12");
        bobJwt   = registerAndGetJwt("bob",   "bob@test.local",   "bob-password-12");

        privateEventId = createEvent(aliceJwt, true, "Alice Private");
        shareToken = issueShare(aliceJwt, privateEventId);

        // Create a pending guest request from "Charlie"
        String body = objectMapper.writeValueAsString(Map.of("displayName", "Charlie"));
        MvcResult r = mockMvc.perform(post("/share/{t}/request", shareToken)
            .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        guestToken = objectMapper.readTree(r.getResponse().getContentAsString()).get("guestToken").asText();

        // Locate the pending request id via the creator's listing
        MvcResult list = mockMvc.perform(get("/events/{id}/requests", privateEventId)
            .header("Authorization", "Bearer " + aliceJwt)).andReturn();
        pendingRequestId = objectMapper.readTree(list.getResponse().getContentAsString())
            .get(0).get("id").asLong();
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
    @DisplayName("POST approve — creator approves; attendee row created with status=GOING")
    void approve_createsAttendee() throws Exception {
        mockMvc.perform(post("/events/{id}/requests/{rid}/approve", privateEventId, pendingRequestId)
                .header("Authorization", "Bearer " + aliceJwt))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("GOING"))
            .andExpect(jsonPath("$.identity.kind").value("GUEST"))
            .andExpect(jsonPath("$.identity.displayName").value("Charlie"));
    }

    @Test
    @DisplayName("POST approve — idempotent on already-approved request")
    void approve_idempotent() throws Exception {
        mockMvc.perform(post("/events/{id}/requests/{rid}/approve", privateEventId, pendingRequestId)
            .header("Authorization", "Bearer " + aliceJwt)).andExpect(status().isOk());
        mockMvc.perform(post("/events/{id}/requests/{rid}/approve", privateEventId, pendingRequestId)
            .header("Authorization", "Bearer " + aliceJwt))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST approve — non-creator gets 403")
    void approve_nonCreator_403() throws Exception {
        mockMvc.perform(post("/events/{id}/requests/{rid}/approve", privateEventId, pendingRequestId)
            .header("Authorization", "Bearer " + bobJwt))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST approve — request id from a different event → 404")
    void approve_pathMismatch_404() throws Exception {
        Integer otherEventId = createEvent(aliceJwt, true, "Other");
        mockMvc.perform(post("/events/{id}/requests/{rid}/approve", otherEventId, pendingRequestId)
            .header("Authorization", "Bearer " + aliceJwt))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST decline — sets request status, no attendee row")
    void decline_noAttendee() throws Exception {
        mockMvc.perform(post("/events/{id}/requests/{rid}/decline", privateEventId, pendingRequestId)
            .header("Authorization", "Bearer " + aliceJwt))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/share/{t}/me", shareToken)
            .header("X-Guest-Token", guestToken))
            .andExpect(jsonPath("$.state").value("declined"));
    }

    @Test
    @DisplayName("POST decline — idempotent on already-declined")
    void decline_idempotent() throws Exception {
        mockMvc.perform(post("/events/{id}/requests/{rid}/decline", privateEventId, pendingRequestId)
            .header("Authorization", "Bearer " + aliceJwt));
        mockMvc.perform(post("/events/{id}/requests/{rid}/decline", privateEventId, pendingRequestId)
            .header("Authorization", "Bearer " + aliceJwt))
            .andExpect(status().isNoContent());
    }

    // helpers identical to JoinRequestSubmitIntegrationTest — duplicated here for self-containment
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
