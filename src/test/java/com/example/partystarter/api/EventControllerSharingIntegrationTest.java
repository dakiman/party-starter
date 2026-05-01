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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.transaction.annotation.Transactional
class EventControllerSharingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private String aliceJwt;
    private String bobJwt;
    private Integer privateEventId;

    @BeforeEach
    void setup() throws Exception {
        aliceJwt = registerAndGetJwt("alice", "alice@test.local", "alice-password-12");
        bobJwt = registerAndGetJwt("bob", "bob@test.local", "bob-password-12");

        privateEventId = createPrivateEvent(aliceJwt);
    }

    @AfterEach
    void cleanup() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ─── POST /events/{id}/share ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /events/{id}/share — creator can issue a share link")
    void issueShare_success() throws Exception {
        MvcResult res = mockMvc.perform(post("/events/{id}/share", privateEventId)
                        .header("Authorization", "Bearer " + aliceJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.url").isString())
                .andReturn();

        String body = res.getResponse().getContentAsString();
        String token = objectMapper.readTree(body).get("token").asText();
        String url = objectMapper.readTree(body).get("url").asText();

        assertNotNull(token);
        assertEquals(36, token.length(), "UUID token should be 36 chars (CHAR(36) UUID string)");
        assertTrue(url.endsWith("/shared/" + token), "URL should end with /shared/{token}");
    }

    @Test
    @DisplayName("POST /events/{id}/share — second call returns the same token (idempotent)")
    void issueShare_idempotent() throws Exception {
        String firstToken = postShareAndExtractToken(privateEventId, aliceJwt);
        String secondToken = postShareAndExtractToken(privateEventId, aliceJwt);

        assertEquals(firstToken, secondToken);
    }

    @Test
    @DisplayName("POST /events/{id}/share — non-creator gets 403")
    void issueShare_nonCreator_forbidden() throws Exception {
        mockMvc.perform(post("/events/{id}/share", privateEventId)
                        .header("Authorization", "Bearer " + bobJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /events/{id}/share — non-existent event returns 404")
    void issueShare_notFound() throws Exception {
        mockMvc.perform(post("/events/{id}/share", 999_999)
                        .header("Authorization", "Bearer " + aliceJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /events/{id}/share — unauth gets 401")
    void issueShare_noAuth() throws Exception {
        mockMvc.perform(post("/events/{id}/share", privateEventId))
                .andExpect(status().isUnauthorized());
    }

    // ─── POST /events/{id}/share/rotate ──────────────────────────────────────

    @Test
    @DisplayName("POST /events/{id}/share/rotate — issues a new token; old token 404s")
    void rotateShare_invalidatesOld() throws Exception {
        String oldToken = postShareAndExtractToken(privateEventId, aliceJwt);

        String newToken = postRotateAndExtractToken(privateEventId, aliceJwt);
        assertNotEquals(oldToken, newToken);

        // Old token is dead
        mockMvc.perform(get("/events/share/{token}", oldToken))
                .andExpect(status().isNotFound());

        // New token works
        mockMvc.perform(get("/events/share/{token}", newToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /events/{id}/share/rotate — non-creator gets 403")
    void rotateShare_nonCreator_forbidden() throws Exception {
        postShareAndExtractToken(privateEventId, aliceJwt); // ensure a token exists

        mockMvc.perform(post("/events/{id}/share/rotate", privateEventId)
                        .header("Authorization", "Bearer " + bobJwt))
                .andExpect(status().isForbidden());
    }

    // ─── GET /events/share/{token} ───────────────────────────────────────────

    @Test
    @DisplayName("GET /events/share/{token} — works unauthenticated for a private event")
    void getByShareToken_private_unauth() throws Exception {
        String token = postShareAndExtractToken(privateEventId, aliceJwt);

        mockMvc.perform(get("/events/share/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(privateEventId))
                .andExpect(jsonPath("$.isPrivate").value(true))
                .andExpect(jsonPath("$.creatorUsername").value("alice"));
    }

    @Test
    @DisplayName("GET /events/share/{token} — works for a public event too")
    void getByShareToken_public_unauth() throws Exception {
        Integer publicEventId = createPublicEvent(aliceJwt);
        String token = postShareAndExtractToken(publicEventId, aliceJwt);

        mockMvc.perform(get("/events/share/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPrivate").value(false));
    }

    @Test
    @DisplayName("GET /events/share/{token} — unknown token returns 404")
    void getByShareToken_unknown() throws Exception {
        mockMvc.perform(get("/events/share/{token}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

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

    private Integer createPrivateEvent(String jwt) throws Exception {
        return createEvent(jwt, true, "Alice's Private Party");
    }

    private Integer createPublicEvent(String jwt) throws Exception {
        return createEvent(jwt, false, "Alice's Public Party");
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

    private String postShareAndExtractToken(Integer eventId, String jwt) throws Exception {
        MvcResult res = mockMvc.perform(post("/events/{id}/share", eventId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    private String postRotateAndExtractToken(Integer eventId, String jwt) throws Exception {
        MvcResult res = mockMvc.perform(post("/events/{id}/share/rotate", eventId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }
}
