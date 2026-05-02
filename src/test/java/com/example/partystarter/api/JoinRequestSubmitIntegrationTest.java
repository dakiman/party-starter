package com.example.partystarter.api;

import com.example.partystarter.BaseIntegrationTest;
import com.example.partystarter.repo.AttendeeRepository;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.repo.GuestUserRepository;
import com.example.partystarter.repo.JoinRequestRepository;
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

class JoinRequestSubmitIntegrationTest extends BaseIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private GuestUserRepository guestUserRepository;
    @Autowired private JoinRequestRepository joinRequestRepository;
    @Autowired private AttendeeRepository attendeeRepository;

    private String aliceJwt;
    private String privateShareToken;
    private String publicShareToken;

    @BeforeEach
    void setup() throws Exception {
        aliceJwt = registerAndGetJwt("alice", "alice@test.local", "alice-password-12");
        Integer privateId = createEvent(aliceJwt, true,  "Alice Private");
        Integer publicId  = createEvent(aliceJwt, false, "Alice Public");
        privateShareToken = issueShare(aliceJwt, privateId);
        publicShareToken  = issueShare(aliceJwt, publicId);
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
    @DisplayName("POST /share/{token}/request — guest first-time submit on private event → state=pending + guest_token issued")
    void guestPrivateFirstSubmit() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("displayName", "Bob", "contactNote", "I work with you"));
        mockMvc.perform(post("/share/{token}/request", privateShareToken)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("pending"))
            .andExpect(jsonPath("$.guestToken").isString());
    }

    @Test
    @DisplayName("POST /share/{token}/request — same guest re-submits → already_pending, no new guest_token")
    void guestPrivateAlreadyPending() throws Exception {
        String firstBody = objectMapper.writeValueAsString(Map.of("displayName", "Bob"));
        MvcResult first = mockMvc.perform(post("/share/{token}/request", privateShareToken)
                .contentType(MediaType.APPLICATION_JSON).content(firstBody))
            .andExpect(status().isOk()).andReturn();
        String guestToken = objectMapper.readTree(first.getResponse().getContentAsString())
            .get("guestToken").asText();

        mockMvc.perform(post("/share/{token}/request", privateShareToken)
                .header("X-Guest-Token", guestToken)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("already_pending"))
            .andExpect(jsonPath("$.guestToken").doesNotExist());
    }

    @Test
    @DisplayName("POST /share/{token}/request — guest on public event → state=attending, attendee row created")
    void guestPublicAutoAttend() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("displayName", "Cara"));
        mockMvc.perform(post("/share/{token}/request", publicShareToken)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("attending"))
            .andExpect(jsonPath("$.guestToken").isString());
    }

    @Test
    @DisplayName("POST /share/{token}/request — auth'd user, no body → state=pending, no guest_token")
    void authdUserPrivateSubmit() throws Exception {
        String bobJwt = registerAndGetJwt("bob", "bob@test.local", "bob-password-12");
        mockMvc.perform(post("/share/{token}/request", privateShareToken)
                .header("Authorization", "Bearer " + bobJwt)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("pending"))
            .andExpect(jsonPath("$.guestToken").doesNotExist());
    }

    @Test
    @DisplayName("POST /share/{token}/request — anonymous + no display name → 400")
    void anonNoNameRejected() throws Exception {
        mockMvc.perform(post("/share/{token}/request", privateShareToken)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /share/{token}/request — unknown share token → 404")
    void unknownShareToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("displayName", "Bob"));
        mockMvc.perform(post("/share/{token}/request", "11111111-2222-3333-4444-555555555555")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isNotFound());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

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
