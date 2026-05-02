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

class AttendeeVisibilityIntegrationTest extends BaseIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private GuestUserRepository guestUserRepository;
    @Autowired private JoinRequestRepository joinRequestRepository;
    @Autowired private AttendeeRepository attendeeRepository;

    private String aliceJwt;
    private String bobJwt;
    private Integer publicEventId;
    private Integer privateEventId;
    private Integer privateEventWithBobApprovedId;

    @BeforeEach
    void setup() throws Exception {
        aliceJwt = registerAndGetJwt("alice", "alice@test.local", "alice-password-12");
        bobJwt   = registerAndGetJwt("bob",   "bob@test.local",   "bob-password-12");

        publicEventId  = createEvent(aliceJwt, false, "Alice Public");
        privateEventId = createEvent(aliceJwt, true,  "Alice Private");

        // A second private event where Bob is already an approved attendee
        privateEventWithBobApprovedId = createEvent(aliceJwt, true, "Alice Private B");
        String shareB = issueShare(aliceJwt, privateEventWithBobApprovedId);
        // Bob requests
        mockMvc.perform(post("/share/{t}/request", shareB)
            .header("Authorization", "Bearer " + bobJwt)
            .contentType(MediaType.APPLICATION_JSON).content("{}"));
        Long requestId = objectMapper.readTree(mockMvc.perform(
                get("/events/{id}/requests", privateEventWithBobApprovedId)
                    .header("Authorization", "Bearer " + aliceJwt))
            .andReturn().getResponse().getContentAsString())
            .get(0).get("id").asLong();
        mockMvc.perform(post("/events/{id}/requests/{rid}/approve",
                privateEventWithBobApprovedId, requestId)
            .header("Authorization", "Bearer " + aliceJwt));
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
    @DisplayName("GET /events/{id}/attendees — public event, anonymous → 200")
    void publicAnon200() throws Exception {
        mockMvc.perform(get("/events/{id}/attendees", publicEventId))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /events/{id}/attendees — private event, anonymous → 401")
    void privateAnon401() throws Exception {
        mockMvc.perform(get("/events/{id}/attendees", privateEventId))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /events/{id}/attendees — private event, creator → 200")
    void privateCreator200() throws Exception {
        mockMvc.perform(get("/events/{id}/attendees", privateEventId)
            .header("Authorization", "Bearer " + aliceJwt))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /events/{id}/attendees — private event, approved attendee → 200")
    void privateApprovedAttendee200() throws Exception {
        mockMvc.perform(get("/events/{id}/attendees", privateEventWithBobApprovedId)
            .header("Authorization", "Bearer " + bobJwt))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /events/{id}/attendees — private event, third-party authed → 403")
    void privateThirdParty403() throws Exception {
        mockMvc.perform(get("/events/{id}/attendees", privateEventId)
            .header("Authorization", "Bearer " + bobJwt))
            .andExpect(status().isForbidden());
    }

    // helpers — copy from prior file
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
