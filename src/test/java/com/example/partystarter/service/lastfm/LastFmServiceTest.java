package com.example.partystarter.service.lastfm;

import com.example.partystarter.model.lastfm.LastFmSimilarResponse;
import com.example.partystarter.model.lastfm.LastFmSimilarResponse.LastFmArtist;
import com.example.partystarter.model.lastfm.LastFmSimilarResponse.SimilarArtists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastFmServiceTest {

    @Mock
    private LastFmClient client;

    @Test
    void returnsEmptyListWhenApiKeyMissing() {
        LastFmService service = new LastFmService(client);
        ReflectionTestUtils.setField(service, "apiKey", "");

        List<LastFmArtist> result = service.getSimilarArtistsForSeed("Daft Punk", 10);

        assertThat(result).isEmpty();
        verify(client, never()).getSimilarArtists(anyString(), anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void returnsEmptyListWhenApiKeyNull() {
        LastFmService service = new LastFmService(client);
        ReflectionTestUtils.setField(service, "apiKey", null);

        List<LastFmArtist> result = service.getSimilarArtistsForSeed("Daft Punk", 10);

        assertThat(result).isEmpty();
    }

    @Test
    void delegatesToClientAndUnwrapsResponse() {
        LastFmService service = new LastFmService(client);
        ReflectionTestUtils.setField(service, "apiKey", "test-key");

        LastFmSimilarResponse mockResponse = LastFmSimilarResponse.builder()
                .similarArtists(SimilarArtists.builder()
                        .artists(List.of(
                                LastFmArtist.builder().name("Justice").match("0.9").build(),
                                LastFmArtist.builder().name("Cassius").match("0.8").build()))
                        .build())
                .build();
        when(client.getSimilarArtists(eq("artist.getsimilar"), eq("Daft Punk"), eq("test-key"), eq("json"), eq(20)))
                .thenReturn(mockResponse);

        List<LastFmArtist> result = service.getSimilarArtistsForSeed("Daft Punk", 20);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(LastFmArtist::getName).containsExactly("Justice", "Cassius");
    }

    @Test
    void returnsEmptyListWhenClientThrows() {
        LastFmService service = new LastFmService(client);
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
        when(client.getSimilarArtists(anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new RuntimeException("503 Service Unavailable"));

        List<LastFmArtist> result = service.getSimilarArtistsForSeed("Daft Punk", 10);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyListWhenResponseIsNull() {
        LastFmService service = new LastFmService(client);
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
        when(client.getSimilarArtists(anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(null);

        List<LastFmArtist> result = service.getSimilarArtistsForSeed("Daft Punk", 10);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyListWhenSimilarArtistsFieldIsNull() {
        LastFmService service = new LastFmService(client);
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
        when(client.getSimilarArtists(anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(LastFmSimilarResponse.builder().similarArtists(null).build());

        List<LastFmArtist> result = service.getSimilarArtistsForSeed("Daft Punk", 10);

        assertThat(result).isEmpty();
    }
}
