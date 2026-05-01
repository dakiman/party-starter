package com.example.partystarter.service;

import com.example.partystarter.model.lastfm.LastFmSimilarResponse.LastFmArtist;
import com.example.partystarter.model.spotify.ArtistResponse;
import com.example.partystarter.model.spotify.SearchArtistsResponse;
import com.example.partystarter.service.lastfm.LastFmService;
import com.example.partystarter.service.spotify.SpotifyCaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifySongServiceTest {

    @Mock
    private SpotifyCaller spotifyCaller;

    @Mock
    private LastFmService lastFmService;

    @InjectMocks
    private SpotifySongService service;

    @BeforeEach
    void wireSelfReference() throws Exception {
        // The orchestration calls self::findArtistByName to go through the cache
        // proxy. In a unit test there's no proxy, so we point self at the bare
        // instance — findArtistByName then delegates to spotifyCaller as usual.
        Field self = SpotifySongService.class.getDeclaredField("self");
        self.setAccessible(true);
        self.set(service, service);
    }

    @Test
    void getSimilarArtists_dedupesAndExcludesSeeds() {
        when(lastFmService.getSimilarArtistsForSeed(anyString(), anyInt())).thenAnswer(inv -> {
            String seed = inv.getArgument(0);
            if ("Daft Punk".equals(seed)) {
                return List.of(
                        artist("Justice", "0.9"),
                        artist("Cassius", "0.8"),
                        artist("DAFT PUNK", "1.0") // self-reference, must be excluded
                );
            }
            if ("Justice".equals(seed)) {
                return List.of(
                        artist("Cassius", "0.95"), // duplicate, higher score wins
                        artist("Daft punk", "0.99"), // also a seed (case-insensitive)
                        artist("SebastiAn", "0.7")
                );
            }
            return Collections.emptyList();
        });

        when(spotifyCaller.findArtistByName(anyString())).thenAnswer(inv -> spotifyArtist(inv.getArgument(0)));

        SearchArtistsResponse response = service.getSimilarArtists(List.of("Daft Punk", "Justice"));

        // Seeds excluded; dups merged (Cassius best score = 0.95 from Justice's list)
        // Order should be by score desc: Cassius (0.95), Justice -> excluded, SebastiAn (0.7)
        // Actually Justice IS a seed so it's excluded. Final order: Cassius (0.95), SebastiAn (0.7).
        // Wait - Justice appears as a seed-similarity but Justice itself was a seed input, so excluded.
        // Daft Punk also excluded.
        assertThat(response.getArtists())
                .extracting(ArtistResponse::getName)
                .containsExactly("Cassius", "SebastiAn");
    }

    @Test
    void getSimilarArtists_capsAtReturnLimit() {
        // 12 unique similar artists from one seed → should be capped at 8.
        List<LastFmArtist> twelve = List.of(
                artist("a1", "1.0"), artist("a2", "0.95"), artist("a3", "0.9"),
                artist("a4", "0.85"), artist("a5", "0.8"), artist("a6", "0.75"),
                artist("a7", "0.7"), artist("a8", "0.65"), artist("a9", "0.6"),
                artist("a10", "0.55"), artist("a11", "0.5"), artist("a12", "0.45")
        );
        when(lastFmService.getSimilarArtistsForSeed(anyString(), anyInt())).thenReturn(twelve);
        when(spotifyCaller.findArtistByName(anyString())).thenAnswer(inv -> spotifyArtist(inv.getArgument(0)));

        SearchArtistsResponse response = service.getSimilarArtists(List.of("Seed"));

        assertThat(response.getArtists()).hasSize(8);
        assertThat(response.getArtists())
                .extracting(ArtistResponse::getName)
                .containsExactly("a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8");
    }

    @Test
    void getSimilarArtists_dropsUnresolvedSpotifyMatches() {
        when(lastFmService.getSimilarArtistsForSeed(anyString(), anyInt())).thenReturn(List.of(
                artist("Resolves", "0.9"),
                artist("Ghost", "0.8")  // null from Spotify
        ));
        when(spotifyCaller.findArtistByName("Resolves")).thenReturn(spotifyArtist("Resolves"));
        when(spotifyCaller.findArtistByName("Ghost")).thenReturn(null);

        SearchArtistsResponse response = service.getSimilarArtists(List.of("Seed"));

        assertThat(response.getArtists())
                .extracting(ArtistResponse::getName)
                .containsExactly("Resolves");
        assertThat(response.getTotal()).isEqualTo(1);
    }

    @Test
    void getSimilarArtists_returnsEmptyForEmptyInput() {
        SearchArtistsResponse response = service.getSimilarArtists(List.of());

        assertThat(response.getArtists()).isEmpty();
        assertThat(response.getTotal()).isZero();
        verify(lastFmService, org.mockito.Mockito.never()).getSimilarArtistsForSeed(anyString(), anyInt());
    }

    @Test
    void getSimilarArtists_handlesNullInput() {
        SearchArtistsResponse response = service.getSimilarArtists(null);

        assertThat(response.getArtists()).isEmpty();
        assertThat(response.getTotal()).isZero();
    }

    private static LastFmArtist artist(String name, String match) {
        return LastFmArtist.builder().name(name).match(match).build();
    }

    private static ArtistResponse spotifyArtist(String name) {
        return ArtistResponse.builder().id("id-" + name).name(name).build();
    }
}
