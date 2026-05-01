package com.example.partystarter.service;

import com.example.partystarter.model.lastfm.LastFmSimilarResponse;
import com.example.partystarter.model.spotify.ArtistResponse;
import com.example.partystarter.model.spotify.SearchArtistsResponse;
import com.example.partystarter.model.spotify.SpotifyGetGenresResponse;
import com.example.partystarter.model.spotify.TopTracksResponse;
import com.example.partystarter.service.lastfm.LastFmService;
import com.example.partystarter.service.spotify.SpotifyCaller;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SpotifySongService {

    private static final int LASTFM_PER_SEED_LIMIT = 20;
    private static final int SIMILAR_ARTISTS_RETURN_LIMIT = 8;

    private final SpotifyCaller spotifyCaller;
    private final LastFmService lastFmService;

    @Autowired
    @Lazy
    private SpotifySongService self;

    @Cacheable(cacheNames = "genres")
    public SpotifyGetGenresResponse getGenres() {
        return spotifyCaller.getGenres();
    }

    public SearchArtistsResponse searchArtists(String name) {
        return spotifyCaller.searchArtists(name);
    }

    @Cacheable(cacheNames = "spotifyArtistTopTracks")
    public TopTracksResponse getArtistTopTracks(String artistId) {
        return spotifyCaller.getArtistTopTracks(artistId);
    }

    @Cacheable(cacheNames = "spotifyArtistByName", key = "#name.toLowerCase()")
    public ArtistResponse findArtistByName(String name) {
        return spotifyCaller.findArtistByName(name);
    }

    public SearchArtistsResponse getSimilarArtists(List<String> seedNames) {
        if (seedNames == null || seedNames.isEmpty()) {
            return SearchArtistsResponse.builder().artists(List.of()).total(0).build();
        }

        Set<String> seedKeys = seedNames.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        Map<String, Double> bestScoreByKey = new LinkedHashMap<>();
        Map<String, String> displayNameByKey = new LinkedHashMap<>();

        for (String seed : seedNames) {
            List<LastFmSimilarResponse.LastFmArtist> similar =
                    lastFmService.getSimilarArtistsForSeed(seed, LASTFM_PER_SEED_LIMIT);
            for (LastFmSimilarResponse.LastFmArtist a : similar) {
                if (a.getName() == null) continue;
                String key = a.getName().toLowerCase(Locale.ROOT);
                if (seedKeys.contains(key)) continue;
                double score = parseMatch(a.getMatch());
                bestScoreByKey.merge(key, score, Math::max);
                displayNameByKey.putIfAbsent(key, a.getName());
            }
        }

        List<String> rankedNames = bestScoreByKey.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(SIMILAR_ARTISTS_RETURN_LIMIT)
                .map(e -> displayNameByKey.get(e.getKey()))
                .toList();

        List<ArtistResponse> resolved = rankedNames.parallelStream()
                .map(self::findArtistByName)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        // Preserve Last.fm score ordering
        Map<String, Integer> orderByName = new LinkedHashMap<>();
        for (int i = 0; i < rankedNames.size(); i++) {
            orderByName.put(rankedNames.get(i).toLowerCase(Locale.ROOT), i);
        }
        resolved.sort(Comparator.comparingInt(a -> orderByName.getOrDefault(a.getName().toLowerCase(Locale.ROOT), Integer.MAX_VALUE)));

        return SearchArtistsResponse.builder()
                .artists(resolved)
                .total(resolved.size())
                .build();
    }

    private double parseMatch(String match) {
        if (match == null) return 0.0;
        try {
            return Double.parseDouble(match);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
