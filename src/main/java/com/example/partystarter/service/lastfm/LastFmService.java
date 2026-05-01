package com.example.partystarter.service.lastfm;

import com.example.partystarter.model.lastfm.LastFmSimilarResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LastFmService {

    private static final String METHOD = "artist.getsimilar";
    private static final String FORMAT = "json";

    private final LastFmClient lastFmClient;

    @Value("${application.lastfm.api-key:}")
    private String apiKey;

    @Cacheable(cacheNames = "lastfmSimilarArtists", key = "#seedName.toLowerCase() + ':' + #limit")
    public List<LastFmSimilarResponse.LastFmArtist> getSimilarArtistsForSeed(String seedName, int limit) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Last.fm API key is not configured (application.lastfm.api-key); returning no similar artists for '{}'", seedName);
            return Collections.emptyList();
        }
        try {
            LastFmSimilarResponse response = lastFmClient.getSimilarArtists(METHOD, seedName, apiKey, FORMAT, limit);
            if (response == null || response.getSimilarArtists() == null || response.getSimilarArtists().getArtists() == null) {
                return Collections.emptyList();
            }
            return response.getSimilarArtists().getArtists();
        } catch (Exception e) {
            log.warn("Last.fm getSimilarArtists failed for seed '{}': {}", seedName, e.getMessage());
            return Collections.emptyList();
        }
    }
}
