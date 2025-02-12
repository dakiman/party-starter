package com.example.partystarter.service.spotify;

import com.example.partystarter.model.spotify.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Arrays;

@Service
@AllArgsConstructor
@Slf4j
public class SpotifyCaller {
    private final SpotifyClient spotifyClient;

//    public Object getTrack(String trackId) {
//        return spotifyClient.getTrack(trackId);
//    }
//
//    public Object getFeaturedPlaylists() {
//        return spotifyClient.getFeaturedPlaylists();
//    }

    public SpotifyGetGenresResponse getGenres() {
        return spotifyClient.getGenres();
    }

//    public Object getRecommendations(List<String> genres, List<String> types) {
//        GetRecommendationsResponse recommendations = spotifyClient.getRecommendations(genres, types);
//        log.info("Artist:" + recommendations.getTracks().get(0).getArtists().get(0).getName());
//        return recommendations.getTracks().get(0).getArtists().get(0).getExternalUrls().getSpotify();
//    }

    public SearchArtistsResponse searchArtists(String name) {
        SpotifySearchResponse response = spotifyClient.searchArtists(name, "artist", 10);

        List<ArtistResponse> artists = Arrays.stream(response.getArtists().getItems())
                .map(this::mapToArtistResponse)
                .toList();

        return SearchArtistsResponse.builder()
                .artists(artists)
                .total(response.getArtists().getTotal())
                .build();
    }

    private ArtistResponse mapToArtistResponse(SpotifyArtist artist) {
        return ArtistResponse.builder()
                .id(artist.getId())
                .name(artist.getName())
                .images(artist.getImages().stream()
                        .map(this::mapToImageResponse)
                        .toList())
                .genres(artist.getGenres())
                .followers(artist.getFollowers().getTotal())
                .popularity(artist.getPopularity())
                .spotifyUrl(artist.getExternalUrls().getSpotify())
                .build();
    }

    private ImageResponse mapToImageResponse(Image image) {
        return ImageResponse.builder()
                .url(image.getUrl())
                .height(image.getHeight())
                .width(image.getWidth())
                .build();
    }
}
