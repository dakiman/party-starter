package com.example.partystarter.api;

import com.example.partystarter.model.spotify.ArtistResponse;
import com.example.partystarter.model.spotify.SearchArtistsResponse;
import com.example.partystarter.model.spotify.TopTracksResponse;
import com.example.partystarter.model.spotify.TrackResponse;
import com.example.partystarter.service.SpotifySongService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MusicControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SpotifySongService spotifySongService;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new MusicController(spotifySongService)).build();
    }

    @Test
    void getArtistTopTracks_returnsServiceResponse() throws Exception {
        TopTracksResponse expected = TopTracksResponse.builder()
                .tracks(List.of(TrackResponse.builder()
                        .id("trk1")
                        .name("One More Time")
                        .albumImageUrl("https://i.scdn.co/image/abc")
                        .durationMs(320_000)
                        .build()))
                .build();
        when(spotifySongService.getArtistTopTracks("artist123")).thenReturn(expected);

        TopTracksResponse actual = objectMapper.readValue(
                mockMvc.perform(get("/music/artists/artist123/top-tracks"))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                TopTracksResponse.class);

        verify(spotifySongService, times(1)).getArtistTopTracks("artist123");
        verifyNoMoreInteractions(spotifySongService);
        assertThat(actual.getTracks()).hasSize(1);
        assertThat(actual.getTracks().get(0).getId()).isEqualTo("trk1");
        assertThat(actual.getTracks().get(0).getName()).isEqualTo("One More Time");
    }

    @Test
    void getSimilarArtists_passesSeedListThroughToService() throws Exception {
        SearchArtistsResponse expected = SearchArtistsResponse.builder()
                .artists(List.of(ArtistResponse.builder().id("a1").name("Justice").build()))
                .total(1)
                .build();
        when(spotifySongService.getSimilarArtists(List.of("Daft Punk", "Cassius"))).thenReturn(expected);

        SearchArtistsResponse actual = objectMapper.readValue(
                mockMvc.perform(get("/music/artists/similar?seedNames=Daft Punk,Cassius"))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                SearchArtistsResponse.class);

        verify(spotifySongService, times(1)).getSimilarArtists(List.of("Daft Punk", "Cassius"));
        verifyNoMoreInteractions(spotifySongService);
        assertThat(actual.getTotal()).isEqualTo(1);
        assertThat(actual.getArtists()).hasSize(1);
        assertThat(actual.getArtists().get(0).getName()).isEqualTo("Justice");
    }
}
