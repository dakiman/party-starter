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

class ShareViewerStateIntegrationTest extends BaseIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private GuestUserRepository guestUserRepository;
    @Autowired private JoinRequestRepository joinRequestRepository;
    @Autowired private AttendeeRepository attendeeRepository;

    private String aliceJwt;
    private String privateShareToken;
    private Integer privateEventId;

    @BeforeEach
    void setup() throws Exception {
        aliceJwt = registerAndGetJwt("alice", "alice@test.local", "alice-password-12");
        privateEventId = createEvent(aliceJwt, true, "Private");
        privateShareToken = issueShare(aliceJwt, privateEventId);
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
    @DisplayName("GET /share/{t}/me — anonymous on a private event → not_requested (200, never 401)")
    void anonNotRequested() throws Exception {
        mockMvc.perform(get("/share/{t}/me", privateShareToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("not_requested"))
            .andExpect(jsonPath("$.eventIsPrivate").value(true));
    }

    @Test
    @DisplayName("GET /share/{t}/me — pending → state=pending")
    void pending() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("displayName", "Bob"));
        MvcResult r = mockMvc.perform(post("/share/{t}/request", privateShareToken)
            .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        String guestToken = objectMapper.readTree(r.getResponse().getContentAsString())
            .get("guestToken").asText();

        mockMvc.perform(get("/share/{t}/me", privateShareToken)
                .header("X-Guest-Token", guestToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("pending"));
    }

    @Test
    @DisplayName("GET /share/{t}/me — approved → state=approved + attendeeStatus=GOING")
    void approved() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("displayName", "Bob"));
        String guestToken = objectMapper.readTree(mockMvc.perform(
                post("/share/{t}/request", privateShareToken)
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn()
            .getResponse().getContentAsString()).get("guestToken").asText();

        Long rid = objectMapper.readTree(mockMvc.perform(
                get("/events/{id}/requests", privateEventId)
                    .header("Authorization", "Bearer " + aliceJwt)).andReturn()
            .getResponse().getContentAsString()).get(0).get("id").asLong();
        mockMvc.perform(post("/events/{id}/requests/{rid}/approve", privateEventId, rid)
            .header("Authorization", "Bearer " + aliceJwt));

        mockMvc.perform(get("/share/{t}/me", privateShareToken)
                .header("X-Guest-Token", guestToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("approved"))
            .andExpect(jsonPath("$.attendeeStatus").value("GOING"));
    }

    @Test
    @DisplayName("GET /share/{t}/me — declined → state=declined")
    void declined() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("displayName", "Bob"));
        String guestToken = objectMapper.readTree(mockMvc.perform(
                post("/share/{t}/request", privateShareToken)
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn()
            .getResponse().getContentAsString()).get("guestToken").asText();

        Long rid = objectMapper.readTree(mockMvc.perform(
                get("/events/{id}/requests", privateEventId)
                    .header("Authorization", "Bearer " + aliceJwt)).andReturn()
            .getResponse().getContentAsString()).get(0).get("id").asLong();
        mockMvc.perform(post("/events/{id}/requests/{rid}/decline", privateEventId, rid)
            .header("Authorization", "Bearer " + aliceJwt));

        mockMvc.perform(get("/share/{t}/me", privateShareToken)
                .header("X-Guest-Token", guestToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("declined"));
    }

    @Test
    @DisplayName("GET /share/{t}/me — public event guest attendee → state=attending")
    void attendingPublic() throws Exception {
        Integer publicId = createEvent(aliceJwt, false, "Public");
        String publicShare = issueShare(aliceJwt, publicId);
        String body = objectMapper.writeValueAsString(Map.of("displayName", "Bob"));
        String guestToken = objectMapper.readTree(mockMvc.perform(
                post("/share/{t}/request", publicShare)
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn()
            .getResponse().getContentAsString()).get("guestToken").asText();

        mockMvc.perform(get("/share/{t}/me", publicShare)
                .header("X-Guest-Token", guestToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("attending"))
            .andExpect(jsonPath("$.attendeeStatus").value("GOING"))
            .andExpect(jsonPath("$.eventIsPrivate").value(false));
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
